package ru.tinkoff.telegram.mt.ui;

import android.app.Dialog;

import java.util.ArrayList;
import java.util.List;

import ru.tinkoff.telegram.mt.entities.Card;
import ru.tinkoff.telegram.mt.entities.Commission;
import ru.tinkoff.telegram.mt.entities.User;
import ru.tinkoff.telegram.mt.mtpart.Async;
import ru.tinkoff.telegram.mt.network.params.CommissionParams;
import ru.tinkoff.telegram.mt.network.params.SrcCardParams;
import ru.tinkoff.telegram.mt.network.responses.WaitingConfirmationResponse;

/**
 * @author a.shishkin1
 */


public interface IInteraction extends Async.IRequestExecutor {

    void showLoader(String[] actions);

    void showRegistration();

    void showConfirmation(WaitingConfirmationResponse waitingConfirmationResponse);

    void showSetPin();

    void showChangePinConfirm();

    void showChangePin();

    void showEnterByPin();

    void showChooseUser(ArrayList<User> users);

    void showSendMoney(User recipient);

    void showSendMoneyFromGroup(User recipient);

    void showDefaultErrorMessage(String title, String message, String additionalMessage);

    void showDefaultErrorMessage(String title, String message, String additionalMessage, boolean closeOnOk);

    void showDialog(Dialog dialog);

    void showSettings();

    void showResetMenu();

    void showOfferMenu();

    void showCards();

    void downloadConfig();


    void onGetConfirmationCode(String code);

    void onCardsReady(List<Card> cards);

    void onAttachCard(Card card);

    void onUserSelected(User user);

    void onSuccessSendMoney(String money, String cardId);

    void onSetLinkedCardPrimary(String cardId);

    void onCommission(Commission commission);

    void onChangePin();

    void onWrongPinCode();

    void onPinAttemptsOverLimit(long finishTime, long currentTime);


    void resolveAction();

    void resolveAuthorization();


    void askCommission(CommissionParams.SRC from, CommissionParams.DEST to, String money);

    void sendMoneyToPhone(SrcCardParams card, String phone, String money);

    void sendMoneyToCard(SrcCardParams card, String cardNumber, String money);

    void resetSettings();

}
