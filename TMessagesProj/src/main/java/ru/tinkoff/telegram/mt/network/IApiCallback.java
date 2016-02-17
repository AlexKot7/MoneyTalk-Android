package ru.tinkoff.telegram.mt.network;

import ru.tinkoff.telegram.mt.network.responses.BaseResult;
import ru.tinkoff.telegram.mt.network.responses.ResultCode;

/**
 * @author a.shishkin1
 */


public interface IApiCallback {
// <GENERATED CODE


    void handleSignUp(BaseResult res);

    void handleSession(BaseResult res);

    void handleMobileSavePin(BaseResult res);

    void handleMobileAuth(BaseResult res);

    void handleGetConfirmationCode(BaseResult res);

    void handleTransferAnyCardToAnyPointer(BaseResult res);

    void handleTransferAnyCardToAnyCard(BaseResult res);

    void handleAttachCard(BaseResult res);

    void handleDetachCard(BaseResult res);

    void handleAccountsFlat(BaseResult res);

    void handleSetLinkedCardPrimary(BaseResult res);

    void handlePaymentCommission(BaseResult res);

    void handlePaymentReceipt(BaseResult res);

    void handleNow(BaseResult res);

    void handlePing(BaseResult res);

    void handleConfirm(BaseResult res);

    void handleConfig(BaseResult res);

    void handleResetWallet(BaseResult res);

    void handleResendCode(BaseResult res);

    void handleNotAuthenticatedException(BaseResult res);

    void handleAuthenticationFailedException(BaseResult res);

    void handleUserLockedException(BaseResult res);

    void handleOperationRejectedException(BaseResult res);

    void handleWaitingConfirmationException(BaseResult res);

    void handleRoleEscalationException(BaseResult res);

    void handleInsufficientPrivilegesException(BaseResult res);

    void handleRequestRateLimitExceededException(BaseResult res);

    void handleInvalidRequestDataException(BaseResult res);

    void handleInvalidPasswordException(BaseResult res);

    void handleInternalErrorException(BaseResult res);

    void handleNotIdentificatedException(BaseResult res);

    void handleConfirmationFailedException(BaseResult res);

    void handleBankServiceDisabledException(BaseResult res);

    void handleConfirmationExpiredException(BaseResult res);

    void handleRoleNotGrantedException(BaseResult res);

    void handleWrongConfirmationCodeException(BaseResult res);

    void handleWrongOperationTicketException(BaseResult res);

    void handleResendFailedException(BaseResult res);

    void handleWrongPinCodeException(BaseResult res);

    void handlePinAttempsExceededException(BaseResult res);

    void handlePinIsNotSetException(BaseResult res);

    void handleDeviceAlreadyLinkedException(BaseResult res);

    void handleDeviceLinkNeededException(BaseResult res);

    void handleNoDataFoundException(BaseResult res);

    void handleTokenExpiredException(BaseResult res);



    class NOTIFY {
        public static boolean notify(IApiCallback callback, BaseResult res) {

            ResultCode rc = res.getResultCode();
            if (rc == ResultCode.OK || res.isSpecificDispatch()) {
                switch(res.getWhat()) {
                    case Requests.SIGN_UP:
                        callback.handleSignUp(res);
                        return true;
                    case Requests.SESSION:
                        callback.handleSession(res);
                        return true;
                    case Requests.MOBILE_SAVE_PIN:
                        callback.handleMobileSavePin(res);
                        return true;
                    case Requests.MOBILE_AUTH:
                        callback.handleMobileAuth(res);
                        return true;
                    case Requests.GET_CONFIRMATION_CODE:
                        callback.handleGetConfirmationCode(res);
                        return true;
                    case Requests.TRANSFER_ANY_CARD_TO_ANY_POINTER:
                        callback.handleTransferAnyCardToAnyPointer(res);
                        return true;
                    case Requests.TRANSFER_ANY_CARD_TO_ANY_CARD:
                        callback.handleTransferAnyCardToAnyCard(res);
                        return true;
                    case Requests.ATTACH_CARD:
                        callback.handleAttachCard(res);
                        return true;
                    case Requests.DETACH_CARD:
                        callback.handleDetachCard(res);
                        return true;
                    case Requests.ACCOUNTS_FLAT:
                        callback.handleAccountsFlat(res);
                        return true;
                    case Requests.SET_LINKED_CARD_PRIMARY:
                        callback.handleSetLinkedCardPrimary(res);
                        return true;
                    case Requests.PAYMENT_COMMISSION:
                        callback.handlePaymentCommission(res);
                        return true;
                    case Requests.PAYMENT_RECEIPT:
                        callback.handlePaymentReceipt(res);
                        return true;
                    case Requests.NOW:
                        callback.handleNow(res);
                        return true;
                    case Requests.PING:
                        callback.handlePing(res);
                        return true;
                    case Requests.CONFIRM:
                        callback.handleConfirm(res);
                        return true;
                    case Requests.CONFIG:
                        callback.handleConfig(res);
                        return true;
                    case Requests.RESET_WALLET:
                        callback.handleResetWallet(res);
                        return true;
                    case Requests.RESEND_CODE:
                        callback.handleResendCode(res);
                        return true;
                }
            } else {
                switch(rc) {
                    case NOT_AUTHENTICATED:
                        callback.handleNotAuthenticatedException(res);
                        return true;
                    case AUTHENTICATION_FAILED:
                        callback.handleAuthenticationFailedException(res);
                        return true;
                    case USER_LOCKED:
                        callback.handleUserLockedException(res);
                        return true;
                    case OPERATION_REJECTED:
                        callback.handleOperationRejectedException(res);
                        return true;
                    case WAITING_CONFIRMATION:
                        callback.handleWaitingConfirmationException(res);
                        return true;
                    case ROLE_ESCALATION:
                        callback.handleRoleEscalationException(res);
                        return true;
                    case INSUFFICIENT_PRIVILEGES:
                        callback.handleInsufficientPrivilegesException(res);
                        return true;
                    case REQUEST_RATE_LIMIT_EXCEEDED:
                        callback.handleRequestRateLimitExceededException(res);
                        return true;
                    case INVALID_REQUEST_DATA:
                        callback.handleInvalidRequestDataException(res);
                        return true;
                    case INVALID_PASSWORD:
                        callback.handleInvalidPasswordException(res);
                        return true;
                    case INTERNAL_ERROR:
                        callback.handleInternalErrorException(res);
                        return true;
                    case NOT_IDENTIFICATED:
                        callback.handleNotIdentificatedException(res);
                        return true;
                    case CONFIRMATION_FAILED:
                        callback.handleConfirmationFailedException(res);
                        return true;
                    case BANK_SERVICE_DISABLED:
                        callback.handleBankServiceDisabledException(res);
                        return true;
                    case CONFIRMATION_EXPIRED:
                        callback.handleConfirmationExpiredException(res);
                        return true;
                    case ROLE_NOT_GRANTED:
                        callback.handleRoleNotGrantedException(res);
                        return true;
                    case WRONG_CONFIRMATION_CODE:
                        callback.handleWrongConfirmationCodeException(res);
                        return true;
                    case WRONG_OPERATION_TICKET:
                        callback.handleWrongOperationTicketException(res);
                        return true;
                    case RESEND_FAILED:
                        callback.handleResendFailedException(res);
                        return true;
                    case WRONG_PIN_CODE:
                        callback.handleWrongPinCodeException(res);
                        return true;
                    case PIN_ATTEMPS_EXCEEDED:
                        callback.handlePinAttempsExceededException(res);
                        return true;
                    case PIN_IS_NOT_SET:
                        callback.handlePinIsNotSetException(res);
                        return true;
                    case DEVICE_ALREADY_LINKED:
                        callback.handleDeviceAlreadyLinkedException(res);
                        return true;
                    case DEVICE_LINK_NEEDED:
                        callback.handleDeviceLinkNeededException(res);
                        return true;
                    case NO_DATA_FOUND:
                        callback.handleNoDataFoundException(res);
                        return true;
                    case TOKEN_EXPIRED:
                        callback.handleTokenExpiredException(res);
                        return true;
                }
            }
            return false;
        }
    }


// GENERATED CODE/>
}
