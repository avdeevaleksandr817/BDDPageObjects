package ru.netology.test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.netology.data.DataHelper;
import ru.netology.page.DashboardPage;
import ru.netology.page.LoginPage;
import ru.netology.page.TransferPage;

import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.netology.data.DataHelper.*;

public class MoneyTransferTest {

    @AfterEach
    void returnCardBalancesToDefault() {
        //открыть страницу
        open("http://localhost:9999");
        //создать обьект страницы логина
        var loginPage = new LoginPage();
        //получить данные авторизации
        var authInfo = DataHelper.getAuthInfo();
        //ввести валидный логин
        var verificationPage = loginPage.validLogin(authInfo);
        //ввести валидный код авторизации
        var verificationCode = DataHelper.getVerificationCode();
        //вернуть страницу авторизации
        verificationPage.validVerify(verificationCode);
        //вернуть обьект DashboardPage
        var dashboardPage = new DashboardPage();
        //баланс первой карты на странице DashboardPage вызываем метод баланс карты(из DataHelper получаем номер первой карты и ее TestId)
        var firstCardBalance = dashboardPage.getCardBalance(getFirstCardInfo());
        //баланс второй карты на странице DashboardPage вызываем метод баланс карты(из DataHelper получаем номер второй карты и ее TestId)
        var secondCardBalance = dashboardPage.getCardBalance(getSecondCardInfo());
        //если баланс первой карты меньше баланса второй
        if (firstCardBalance < secondCardBalance) {
            //на странице дашборда кликаем по кнопке пополнить на первой карте
            dashboardPage.selectCardToTransfer(DataHelper.getFirstCardInfo());
            //создаем обьект страницы перевода денег
            var transferPage = new TransferPage();
            //на странице перевода вызываем метод валидной транзакции
            transferPage.makeValidTransfer(String.valueOf((secondCardBalance - firstCardBalance) / 2),
                    //DataHelper получаем номер второй карты и ее TestId
                    DataHelper.getSecondCardInfo()).getCardBalance(getSecondCardInfo());
            //иначе если баланс первой карты больше баланса второй
        } else if (firstCardBalance > secondCardBalance) {
            //на странице дашборда кликаем по кнопке пополнить на второй карте
            dashboardPage.selectCardToTransfer(DataHelper.getSecondCardInfo());
            //создаем обьект страницы перевода денег
            var transactionPage = new TransferPage();
            //на странице перевода создаем валидный перевод
            transactionPage.makeValidTransfer(String.valueOf((firstCardBalance - secondCardBalance) / 2),
                    //DataHelper получаем номер первой карты и ее TestId
                    DataHelper.getFirstCardInfo()).getCardBalance(getFirstCardInfo());
        }
    }

    @Test
    @DisplayName("Happy Path  " + "  Transfer from first to second card")
    void transferFromFirstToSecondCard() {

        var loginPage = open("http://localhost:9999", LoginPage.class);
        //получаем данные аутентификации
        var authInfo = DataHelper.getAuthInfo();
        //страница верификации через страницу логина
        var verificationPage = loginPage.validLogin(authInfo);
        //получаем код верификации
        var verificationCode = getVerificationCode();
        //на странице верификации (вводим код нажимаем кнопку и возвращаем новый обьект дашборда) и сохраняем это в переменную
        var dashboardPage = verificationPage.validVerify(verificationCode);
        //получаем информацию из DataHelper о первой и второй карте
        var firstCardInfo = getFirstCardInfo();
        var secondCardInfo = DataHelper.getSecondCardInfo();
        //через dashboardPage получаем данные баланса первой и второй карты
        var firstCardBalance = dashboardPage.getCardBalance(firstCardInfo);
        var secondCardBalance = dashboardPage.getCardBalance(secondCardInfo);
        //генерируем валидную сумму перевода
        var amount = generateValidAmount(firstCardBalance);
        //считаем ожидаемые балансы карт после перевода
        var expectedBalanceFirstCard = firstCardBalance - amount;
        var expectedBalanceSecondCard = secondCardBalance + amount;
        //выбираем карту для перевода, передаем информацию о карте на которую надо перевести средства
        var transferPage = dashboardPage.selectCardToTransfer(secondCardInfo);
        //выполняем перевод и возвращаем страницу
        dashboardPage = transferPage.makeValidTransfer(String.valueOf(amount), firstCardInfo);
        //получаем балансы карт после перевода
        var actualBalanceFirstCard = dashboardPage.getCardBalance(firstCardInfo);
        var actualBalanceSecondCard = dashboardPage.getCardBalance(secondCardInfo);
        //сравниваем ожидаемые и актуальные результаты
        assertEquals(expectedBalanceFirstCard, actualBalanceFirstCard);
        assertEquals(expectedBalanceSecondCard, actualBalanceSecondCard);

    }

    @Test
    @DisplayName("transfer From Second To First Card")
    void transferFromSecondToFirstCard() {

        var loginPage = open("http://localhost:9999", LoginPage.class);
        var authInfo = DataHelper.getAuthInfo();
        var verificationPage = loginPage.validLogin(authInfo);
        var verificationCode = getVerificationCode();
        var dashboardPage = verificationPage.validVerify(verificationCode);
        var firstCardInfo = getFirstCardInfo();
        var secondCardInfo = DataHelper.getSecondCardInfo();
        var firstCardBalance = dashboardPage.getCardBalance(firstCardInfo);
        var secondCardBalance = dashboardPage.getCardBalance(secondCardInfo);
        var amount = generateValidAmount(secondCardBalance);
        var expectedBalanceSecondCard = secondCardBalance - amount;
        var expectedBalanceFirstCard = firstCardBalance + amount;
        var transferPage = dashboardPage.selectCardToTransfer(firstCardInfo);
        dashboardPage = transferPage.makeValidTransfer(String.valueOf(amount), secondCardInfo);
        var actualBalanceSecondCard = dashboardPage.getCardBalance(secondCardInfo);
        var actualBalanceFirstCard = dashboardPage.getCardBalance(firstCardInfo);
        assertEquals(expectedBalanceFirstCard, actualBalanceFirstCard);
        assertEquals(expectedBalanceSecondCard, actualBalanceSecondCard);

    }

}

