package ru.tinkoff.telegram.mt.utils.formatting;


import ru.tinkoff.telegram.mt.utils.formatting.formatters.AFormatWatcher;

public interface FormattedTextChangeListener {
    void onTextFormatted(AFormatWatcher formatter, String newFormattedText);
}