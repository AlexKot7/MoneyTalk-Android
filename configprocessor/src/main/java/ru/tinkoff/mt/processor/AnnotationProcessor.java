package ru.tinkoff.mt.processor;


import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static javax.lang.model.type.TypeKind.*;

/**
 * @author Mikhail Artemyev
 *         <p/>
 *         <p/>
 *         <mt-config>
 *         <variable name="API_VERSION">56</variable>
 *         <variable name="SECRET_CODE">42</variable>
 *         <class name="ImportantClass">
 *         <variable name="PASSWORD">qwerty</variable>
 *         </class>
 *         </mt-config>
 *
 *         Path to the configuration file is stored in the MT_CONFIG_PATH environment variable.
 *         This path can be either absolute path of a local file or URL (http(s) or ftp) of a
 *         remote one.
 *
 *         If server with remote config file requeres basic authorization than login and password
 *         should be stored in the MT_LOGIN_PASSWORD environment variable separated with colon (:)
 *         e.g. user:password
 */
@SupportedAnnotationTypes(value = {"ru.tinkoff.mt.processor.InjectFromConfig"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class AnnotationProcessor extends AbstractProcessor {

    private static final String CONFIG_PATH_ENVIRONMENT_VARIABLE = "MT_CONFIG_PATH";
    private static final String LOGIN_PASSWORD_ENVIRONMENT_VARIABLE = "MT_LOGIN_PASSWORD";
    private static final String PROXY = "MT_PROXY";
    private static final String XML_ROOT_ELEMENT = "config";
    private static final String XML_ATTR_NAME = "name";
    private static final String XML_TAG_VARIABLE = "variable";
    private static final String XML_TAG_CLASS = "class";

    private JavacProcessingEnvironment processingEnvironment;
    private TreeMaker maker;

    private NodeList rootVariables;
    private Map<String, NodeList> rootClasses;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.processingEnvironment = (JavacProcessingEnvironment) processingEnv;
        this.maker = TreeMaker.instance(processingEnvironment.getContext());
        readConfig();
    }

    private boolean readConfig() {
        final String configPath = System.getenv(CONFIG_PATH_ENVIRONMENT_VARIABLE);
        if (configPath == null || configPath.isEmpty()) {
            log(Diagnostic.Kind.WARNING, "Configuration file for MT annotation processor isn't specified");
            return false;
        }

        InputStream configStream = null;
        try {
            configStream = inputStreamFromPath(configPath);
            final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            final Document configXml = builder.parse(configStream);
            final org.w3c.dom.Element configRoot = configXml.getDocumentElement();

            if(!XML_ROOT_ELEMENT.equals(configRoot.getNodeName().toString())){
                log(Diagnostic.Kind.ERROR, "Wrong root xml element");
            } else {
                readConfigValues(configRoot);
            }

            configStream.close();
        } catch (FileNotFoundException e) {
            log(Diagnostic.Kind.ERROR, "Cannot find configuration file: " + configPath);
            return false;
        } catch (ParserConfigurationException | SAXException | IOException | IllegalStateException e) {
            log(Diagnostic.Kind.ERROR, e, "Error parsing configuration file");
            closeQuietly(configStream);
            return false;
        } catch (Exception e) {
            log(Diagnostic.Kind.ERROR, e, "Exception on read config");
            closeQuietly(configStream);
            return false;
        }

        return true;
    }

    private InputStream inputStreamFromPath(final String path) throws IOException, IllegalStateException {
        if(path == null){
            throw new IllegalArgumentException("Config path cannot be null");
        }

        if(path.startsWith("http") || path.startsWith("ftp")){
            log(Diagnostic.Kind.NOTE, "Obtaining configuration file from %s", path);
            final String authorization = System.getenv(LOGIN_PASSWORD_ENVIRONMENT_VARIABLE);

            String proxyData = System.getenv(PROXY);
            HttpURLConnection connection;
            if(proxyData != null) {
                String[] parts = proxyData.trim().split(":");
                final String[] authParts = authorization.trim().split(":");
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(parts[0], Integer.parseInt(parts[1])));
                Authenticator authenticator = new Authenticator() {

                    public PasswordAuthentication getPasswordAuthentication() {
                        return (new PasswordAuthentication(authParts[0],
                                authParts[1].toCharArray()));
                    }
                };
                Authenticator.setDefault(authenticator);
                connection = (HttpURLConnection) new URL(path).openConnection(proxy);
            } else {
                connection = (HttpURLConnection) new URL(path).openConnection();
            }

            if(authorization != null) {
                final String encoded = DatatypeConverter.printBase64Binary(authorization.getBytes());
                connection.setRequestProperty("Authorization", "Basic "+encoded);
            }
            return connection.getInputStream();
        } else {
            log(Diagnostic.Kind.NOTE, "Reading local configuration file %s", path);
            return new FileInputStream(path);
        }
    }

    private void readConfigValues(final org.w3c.dom.Element configRoot) {
        if (configRoot == null) {
            return;
        }

        this.rootVariables = configRoot.getElementsByTagName(XML_TAG_VARIABLE);

        final NodeList classes = configRoot.getElementsByTagName(XML_TAG_CLASS);
        if (classes == null) {
            return;
        }

        rootClasses = new HashMap<>();
        for (int index = 0; index < classes.getLength(); index++) {
            final Node classNode = classes.item(index);
            if (classNode.getChildNodes() == null || classNode.getChildNodes().getLength() == 0) {
                continue;
            }

            final String className = getXmlAttribute(classNode, XML_ATTR_NAME);
            if (className != null) {
                rootClasses.put(className, classNode.getChildNodes());
            }
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (rootClasses == null && rootVariables == null) {
            log(Diagnostic.Kind.NOTE, "Skipping MT annotation processing. Because there's no config file");
            return true;
        }

        if (annotations == null || annotations.isEmpty()) {
            return true;
        }

        log(Diagnostic.Kind.NOTE, "Annotation processor started!");

        final JavacElements utils = processingEnvironment.getElementUtils();
        final Set<? extends Element> fieldsSet = roundEnv.getElementsAnnotatedWith(InjectFromConfig.class);
        for (final Element field : fieldsSet) {
            final JCTree node = utils.getTree(field);
            if (node instanceof JCTree.JCVariableDecl) {
                processVariable((JCTree.JCVariableDecl) node, rootVariables);
            } else if (node instanceof JCTree.JCClassDecl) {
                processClass((JCTree.JCClassDecl) node);
            }
        }

        return true;
    }

    private void processClass(final JCTree.JCClassDecl classDecl) {
        final String className = classDecl.sym.fullname.toString();
        if (!rootClasses.containsKey(className)) {
            return;
        }

        final NodeList configVariablesValues = rootClasses.get(className);
        final List<JCTree> classMembersList = classDecl.getMembers();
        for (final JCTree member : classMembersList) {
            if (member instanceof JCTree.JCVariableDecl) {
                processVariable((JCTree.JCVariableDecl) member, configVariablesValues);
            }
        }
    }


    private void processVariable(JCTree.JCVariableDecl variable,
                                 NodeList fromNodeList) {
        final String configVariableName = variable.getName().toString();
        final Object configVariableValue = getNodeValueFromConfig(configVariableName, variable.vartype.type, fromNodeList);
        if (configVariableValue != null) {
            variable.init = maker.Literal(configVariableValue);
            log(Diagnostic.Kind.NOTE, "Value of '%s' is updated from config", variable.getName().toString());
        }
    }

    private void log(final Diagnostic.Kind king, final String message, final String... args) {
        log(king, null, message, args);
    }

    private void log(final Diagnostic.Kind king, final Throwable ex, final String message, final String... args) {
        final String formattedMessage = String.format(message, args);
        String printable = formattedMessage;
        if (ex != null) {
            printable += ". Cause: ";
        }

        processingEnvironment.getMessager().printMessage(king, printable);

        if (ex != null) {
            processingEnvironment.getMessager().printMessage(king, ex.getClass().getName());
            for (final StackTraceElement element : ex.getStackTrace()) {
                processingEnvironment.getMessager().printMessage(king, element.toString());
            }
        }
    }

    private void closeQuietly(final InputStream inputStream) {
        if (inputStream == null) {
            return;
        }

        try {
            inputStream.close();
        } catch (IOException e) {
            log(Diagnostic.Kind.WARNING, e, "Error trying to close quietly");
        }
    }

    private  Object getNodeValueFromConfig(final String variableName, final Type varType, final NodeList nodeList) {
        if (nodeList == null || nodeList.getLength() == 0) {
            log(Diagnostic.Kind.WARNING, "Trying to get value of a node that is not in the config. Result will be 'null'");
            return null;
        }

        if (variableName == null || variableName.isEmpty()) {
            log(Diagnostic.Kind.WARNING, "Trying to get value of a noname node. Result will be 'null'");
            return null;
        }

        for (int index = 0; index < nodeList.getLength(); index++) {
            final Node node = nodeList.item(index);
            final String nodeName = getXmlAttribute(node, XML_ATTR_NAME);
            if (variableName.equals(nodeName)) {
                return castValue(node.getTextContent(), varType);
            }
        }

        return null;
    }

    private Object castValue(final String valueIn, final Type type){
        if(valueIn == null || valueIn.length() == 0 || type == null){
            return null;
        }

        switch (type.getKind()){
            case BOOLEAN:
                return "true".equals(valueIn) ? true : "false".equals(valueIn) ? false : null;
            case INT:
                return Integer.valueOf(valueIn);
            case LONG:
                return Long.valueOf(valueIn);
            case FLOAT:
                return Float.valueOf(valueIn);
            case DOUBLE:
                return Double.valueOf(valueIn);
            case BYTE:
                return Byte.valueOf(valueIn);
            case CHAR:
                return Character.valueOf(valueIn.charAt(0));
            case SHORT:
                return Short.valueOf(valueIn);
            default:
                return valueIn;
        }
    }

    private String getXmlAttribute(final Node node, final String attribute) {
        if (!node.hasAttributes()) {
            return null;
        }

        final Node nameNode = node.getAttributes().getNamedItem(attribute);
        if (nameNode == null) {
            return null;
        }

        return nameNode.getNodeValue();
    }
}
