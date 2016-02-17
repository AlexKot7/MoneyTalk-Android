package ru.tinkoff.telegram.mt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import org.telegram.messenger.ContactsController;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.ChatActivityEnterView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;

import ru.tinkoff.telegram.mt.base.IConverter;
import ru.tinkoff.telegram.mt.entities.User;
import ru.tinkoff.telegram.mt.mtpart.MtAppPart;
import ru.tinkoff.telegram.mt.ui.BaseActivity;
import ru.tinkoff.telegram.mt.ui.MtActivity;
import ru.tinkoff.telegram.mt.utils.Analytics;
import ru.tinkoff.telegram.mt.views.PagesView;
import ru.tinkoff.telegram.mt.views.SendMoneyButton;

/**
 * @author a.shishkin1
 */


public class Glue {

    public static boolean DEBUG = BuildConfig.DEBUG;

    private static final String MT_PREF_LOCALIZATION_INITIATED = "locale_added";
    private static final String RUSSIAN_LOCALE_FILENAME = "Russian.xml";

    private static final int mtActivityRequestCode = 1676; // magic number
    private static final int menuIndex = 18989; // magic number

    public static String getMyPhone() {
        TLRPC.User user = UserConfig.getCurrentUser();
        return user == null ? null : user.phone;
    }

    public static String getMyName() {
        TLRPC.User user = UserConfig.getCurrentUser();
        String name = ContactsController.formatName(user.first_name, user.last_name);
        if(name != null) {
            return name;
        }
        return user.username;
    }

    public static int attach_button_res_id = R.drawable.mt_add_ruble;

    /**
     * отправка сообщения после совершения перевода
     * {@link org.telegram.ui.ChatActivity#onActivityResultFragment(int, int, Intent)}
     *
     * @param sender - отправщик сообщений, в данном случае  {@link org.telegram.ui.Components.ChatActivityEnterView}
     */
    public static void onActivityResult(Intent intent, int requestCode, int resultCode, ChatActivityEnterView sender) {
        if (requestCode != mtActivityRequestCode || resultCode != Activity.RESULT_OK)
            return;
        if (sender == null) {
            throw new NullPointerException("sender is null");
        }
        String text = intent.getStringExtra(Intent.EXTRA_TEXT);
        sender.processSendingText(text);
    }

    /**
     * menu indexes in {@link org.telegram.ui.SettingsActivity}
     */
    private static int startPosition = 7;
    private static int rawMoneySettingCards = startPosition;

    private static int[] allAdditionalMenuItems = new int[]{ rawMoneySettingCards};


    /**
     * Обертка для  {@link org.telegram.ui.SettingsActivity.ListAdapter}
     * использующегося на экране настроек в Телеграме
     */
    public static class ListAdapterWrapper extends BaseAdapter {

        private BaseAdapter inner;
        private Context context;

        public ListAdapterWrapper(BaseAdapter inner, Context context) {
            this.inner = inner;
            this.context = context;
        }

        @Override
        public boolean isEnabled(int position) {
            if (position == rawMoneySettingCards)
                return true;
            return inner.isEnabled(recalculatePosition(position));
        }

        @Override
        public int getCount() {
            return inner.getCount() + allAdditionalMenuItems.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            if (position == rawMoneySettingCards)
                return position;
            return inner.getItemId(recalculatePosition(position));
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (position == rawMoneySettingCards) {
                if (convertView == null) {
                    convertView = new TextSettingsCell(context);
                }

                ((TextSettingsCell) convertView).setText(parent.getContext().getString(R.string.mt_gen_settings_moneytalk_buttontitle), true);
                return convertView;
            }

            return inner.getView(recalculatePosition(position), convertView, parent);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == rawMoneySettingCards)
                return 2; // as simple item
            return inner.getItemViewType(recalculatePosition(position));
        }

        @Override
        public int getViewTypeCount() {
            return inner.getViewTypeCount();
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {
            if (observer != null) {
                inner.registerDataSetObserver(observer);
            }
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            if (observer != null) {
                inner.unregisterDataSetObserver(observer);
            }
        }
    }

    private static int recalculatePosition(int position) {
        return position < startPosition ? position : position - allAdditionalMenuItems.length;
    }

    /**
     * Обертка onItemClickListener для ListView использующегося на экране настроек
     * {@link org.telegram.ui.SettingsActivity#listView }
     */
    public static class OnItemClickListenerWrapper implements AdapterView.OnItemClickListener {

        private AdapterView.OnItemClickListener inner;

        public OnItemClickListenerWrapper(AdapterView.OnItemClickListener inner) {
            this.inner = inner;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position == rawMoneySettingCards) {
                Intent intent = new Intent(view.getContext(), MtActivity.class);
                intent.setAction(MtActivity.ACTION_SETTINGS);
                intent.putExtra(MtActivity.EXTRA_PHONE, getMyPhone());
                intent.putExtra(MtActivity.EXTRA_NAME, getMyName());
                intent.putExtra(BaseActivity.EXTRA_ACTION_BAR_HEIGHT, ActionBar.getCurrentActionBarHeight());
                view.getContext().startActivity(intent);
                return;
            }
            inner.onItemClick(parent, view, recalculatePosition(position), id);
        }
    }


    private static ArrayList<User> getUsers(BaseFragment fragment) throws Exception {
        ArrayList<User> res = new ArrayList<>();
        ChatActivity chatActivity = (ChatActivity) fragment;

        Field uf = chatActivity.getClass().getDeclaredField("currentUser");
        uf.setAccessible(true);
        TLRPC.User user = (TLRPC.User) uf.get(chatActivity);

        IConverter<TLRPC.User, User> converter = new IConverter<TLRPC.User, User>() {
            @Override
            public User convert(TLRPC.User source) {
                String name = ContactsController.formatName(source.first_name, source.last_name);
                return new User(name, source.phone);
            }
        };

        if (user != null) {
            res.add(converter.convert(user));
        } else {
            Field f = chatActivity.getClass().getDeclaredField("info");
            f.setAccessible(true);
            TLRPC.ChatFull chatFull = (TLRPC.ChatFull) f.get(chatActivity);
            TLRPC.ChatParticipants info = chatFull.participants;

            for (int i = 0; i < info.participants.size(); i++) {
                TLRPC.User u = MessagesController.getInstance().getUser(info.participants.get(i).user_id);
                res.add(converter.convert(u));
            }
        }

        return res;
    }

    ;


    public static class Intro {
        public static final int itemCount = 3;

        public static final int[] titles = new int[]{R.string.mt_intro_title_1, R.string.mt_intro_title_2, R.string.mt_intro_title_3};
        public static final int[] icons = new int[]{R.drawable.mt_intro_1_, R.drawable.mt_intro_2_, R.drawable.mt_intro_3_};
        public static final int[] messages = new int[]{R.string.mt_intro_message_1, R.string.mt_intro_message_2, R.string.mt_intro_message_3};


        public static void injectPagerView(ViewGroup vg) {
            vg.removeAllViews();
            PagesView pv = new PagesView(vg.getContext());
            pv.setValue(1);
            pv.setCount(itemCount);
            vg.addView(pv);
        }

        public static void resetValue(ViewGroup vg, int value) {
            View view = vg.getChildAt(0);
            if (view == null || !(view instanceof PagesView))
                return;
            ((PagesView) view).setValue(value);
        }
    }


    /**
     * MT Warning: Glue#initializeLocalization() should be called AFTER ApplicationLoader#applicationContext is initialized
     */
    public static void initializeLocalization(final Context context) {
        final SharedPreferences mtPrefs = context.getSharedPreferences(MtAppPart.MT_PREFERENCES_NAME, Context.MODE_PRIVATE);
        boolean localeInit = mtPrefs.getBoolean(MT_PREF_LOCALIZATION_INITIATED, false);
        if (localeInit) {
            return;
        }

        // init LocaleController to load default language
        LocaleController.getInstance();

        // find out currently used language
        final LocaleController.LocaleInfo tlgLocale = getCurrentLocaleInfo();
        if (tlgLocale == null) {
            return;
        }

        // And system language
        final String systemLang = LocaleController.getInstance().getSystemDefaultLocale().getLanguage();

        // load russian locale. It's also will be set as current Telegram language
        final boolean russianLangApplied = loadRussianLocale(context);
        if (!russianLangApplied) {
            return;
        }

        // if russian is not a default system language then roll back to the language that was
        // initially set as current
        if (!systemLang.equals("ru")) {
            LocaleController.getInstance().applyLanguage(tlgLocale, true, false);
        }

        SharedPreferences.Editor editor = mtPrefs.edit();
        editor.putBoolean(MT_PREF_LOCALIZATION_INITIATED, true);
        editor.commit();

    }

    static private LocaleController.LocaleInfo getCurrentLocaleInfo() {
        try {
            final Field localeInfoField = LocaleController.class.getDeclaredField("currentLocaleInfo");
            localeInfoField.setAccessible(true);
            return (LocaleController.LocaleInfo) localeInfoField.get(LocaleController.getInstance());
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Cannot access private field LocaleController#currentLocaleInfo", e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot access private field LocaleController#currentLocaleInfo", e);
        }
    }

    private static boolean loadRussianLocale(final Context context) {

        final String russianLocaleFilename = copyRussianLocaleFromResources(context);
        if (russianLocaleFilename == null) {
            return false;
        }

        final File localeFile = new File(russianLocaleFilename);
        return LocaleController.getInstance().applyLanguageFile(localeFile);
    }

    private static String copyRussianLocaleFromResources(final Context context) {
        try {
            final InputStream is = context.getAssets().open(RUSSIAN_LOCALE_FILENAME,
                    AssetManager.ACCESS_BUFFER);

            final OutputStream os = context.openFileOutput(RUSSIAN_LOCALE_FILENAME, Context.MODE_PRIVATE);
            final byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }

            is.close();
            os.close();

            return String.format("%s/%s", context.getFilesDir().getAbsolutePath(), RUSSIAN_LOCALE_FILENAME);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


    public static void insertSendMoneyButtonTo(View[] views, ViewGroup parent, BaseFragment fragment) {
        if (views[8] instanceof SendMoneyButton) {
            return;
        }

        // remove photos recycler (views[8]) view and progress view (views [9])
        for(int i = 8; i <= 9; i++) {
            int ind = parent.indexOfChild(views[i]);
            if (ind >= 0) {
                parent.removeViewAt(ind);
            }
        }

        final SendMoneyButton buttonSendMoney = new SendMoneyButton(parent.getContext());
        buttonSendMoney.setOnClickListener(new SendButtonClickListener(fragment));
        parent.addView(buttonSendMoney);

        views[8] = buttonSendMoney;
    }

    public static void layoutAttachViewItems(View[] views, float density) {
        final int x = views[0].getLeft() + dp(density, (10 + 5));
        final int tail = views[3].getRight() - dp(density, (10 + 5));

        final int containerHeight = dp(density, 96);
        final int height = views[8].getMeasuredHeight();
        final int y = (containerHeight >> 1) - (height >> 1);
        views[8].layout(x, y, tail, y + height);
    }

    private static int dp(final float density, final float value){
        if(value == 0 || density == 0){
            return 0;
        }

        return (int) Math.ceil(density * value);
    }

    public static class SendButtonClickListener implements View.OnClickListener {
        private final BaseFragment fragment;

        public SendButtonClickListener(BaseFragment fragment) {
            this.fragment = fragment;
        }

        @Override
        public void onClick(View v) {
            if (fragment.getParentActivity() == null) {
                return;
            }
            Context context = v.getContext();
            if(!(context instanceof Activity)) {
                Analytics.getInstance(context).trackFatalException(new ClassCastException("in Glue SendButtom Context is not Activity (o rly?)")); // not expected
                return;
            }

            Activity activity = (Activity) context;
            ArrayList<User> users = new ArrayList<>();
            try {
                users = getUsers(fragment);
            } catch (Exception e) {
                Exception ex = new Exception("in Glue on getUsers ", e);
                Analytics.getInstance(activity).trackFatalException(ex);
            }

            Intent intent = new Intent(fragment.getParentActivity(), MtActivity.class);
            intent.putExtra(MtActivity.EXTRA_PHONE, getMyPhone());
            intent.putExtra(MtActivity.EXTRA_NAME, getMyName());
            intent.putParcelableArrayListExtra(MtActivity.EXTRA_USERS, users);
            intent.setAction(MtActivity.ACTION_SEND_MONEY);
            intent.putExtra(BaseActivity.EXTRA_ACTION_BAR_HEIGHT, ActionBar.getCurrentActionBarHeight());
            activity.startActivityForResult(intent, mtActivityRequestCode);
        }
    }


    public static String getAppName(Context  context) {
        return context.getString(R.string.mt_app_name);
    }

    public static String getHarcodedFilesPath() {
        return String.format("/data/data/%s/files", BuildConfig.APPLICATION_ID);
    }

}
