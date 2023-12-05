package com.lfcounago.gastoscompartidos;

import java.util.HashMap;
import java.util.Map;

public class CurrencySymbol {

    private static Map<String, String> currencySymbol;

    static {
        currencySymbol = new HashMap<>();
        currencySymbol.put("Euro - EUR", "€");
        currencySymbol.put("Baht tailandés - THB", "฿");
        currencySymbol.put("Bolívar fuerte de Venezuela - VEF", "Bs.S.");
        currencySymbol.put("Boliviano Bolivia - BOB", "Bs.");
        currencySymbol.put("Chelin keniano - KES", "Ksh");
        currencySymbol.put("Colón costarricense - CRC", "₡");
        currencySymbol.put("Córdoba nicaragüense - NIO", "C$");
        currencySymbol.put("Corona checa - CZK", "Kč");
        currencySymbol.put("Corona danesa - DKK", "kr");
        currencySymbol.put("Corona islandesa - ISK", "kr");
        currencySymbol.put("Corona sueca - SEK", "kr");
        currencySymbol.put("Dalasi Gambia - GMD", "D");
        currencySymbol.put("Dinar argelino - DZD", "د.ج");
        currencySymbol.put("Dinar bahreiní - BHD", "BD");
        currencySymbol.put("Dinar jordano - JOD", "JD");
        currencySymbol.put("Dinar kuwaití - KWD", "KD");
        currencySymbol.put("Dinar serbio - RSD", "дин.");
        currencySymbol.put("Dinar tunecino - TND", "د.ت");
        currencySymbol.put("Dírham Em. Árabes - AED", "د.إ");
        currencySymbol.put("Dírham marroquí - MAD", "د.م.");
        currencySymbol.put("Dólar americano - USD", "$");
        currencySymbol.put("Dólar australiano - AUD", "A$");
        currencySymbol.put("Dólar Australiano - AUD", "A$");
        currencySymbol.put("Dólar canadiense - CAD", "CA$");
        currencySymbol.put("Dólar de Hong Kong - HKD", "HK$");
        currencySymbol.put("Dólar jamaiquino - JMD", "J$");
        currencySymbol.put("Dólar neozelandés - NZD", "NZ$");
        currencySymbol.put("Dolar singapurense - SGD", "S$");
        currencySymbol.put("Dólar taiwanés - TWD", "NT$");
        currencySymbol.put("Dólar trinitense - TTD", "TT$");
        currencySymbol.put("Dong vietnamita - VND", "₫");
        currencySymbol.put("Florín de Antillas Holandesas - ANG", "ƒ");
        currencySymbol.put("Florín de Aruba - AWG", "Afl.");
        currencySymbol.put("Forint - HUF", "Ft");
        currencySymbol.put("Franco BCEAO - XOF", "CFA");
        currencySymbol.put("Franco BEAC - XAF", "CFA");
        currencySymbol.put("Franco suizo - CHF", "CHF");
        currencySymbol.put("Grivna Ucrania - UAH", "₴");
        currencySymbol.put("Kuna croata - HRK", "kn");
        currencySymbol.put("Kwanza - AOA", "Kz");
        currencySymbol.put("Lek - ALL", "Lek");
        currencySymbol.put("Leu rumano - RON", "RON");
        currencySymbol.put("Lev búlgaro - BGN", "лв.");
        currencySymbol.put("Libra egipcia - EGP", "ج.م");
        currencySymbol.put("Libra escocesa - SCP", "£");
        currencySymbol.put("Libra esterlina - GBP", "£");
        currencySymbol.put("Lira turca - TRY", "₺");
        currencySymbol.put("Lita lituana - LTL", "Lt");
        currencySymbol.put("Manat azerbaiyano - AZM", "ман");
        currencySymbol.put("Marco bosnio - BAM", "KM");
        currencySymbol.put("Nuevo shequel israelí - ILS", "₪");
        currencySymbol.put("Nuevo sol peruano - PEN", "S/.");
        currencySymbol.put("Peso argentino - ARS", "$");
        currencySymbol.put("Peso chileno - CLP", "$");
        currencySymbol.put("Peso colombiano - COP", "$");
        currencySymbol.put("Peso dominicano - DOP", "RD$");
        currencySymbol.put("Peso filipino - PHP", "₱");
        currencySymbol.put("Peso mexicano - MXN", "$");
        currencySymbol.put("Peso uruguayo - UYU", "$U");
        currencySymbol.put("Quetzal guatemalteco - GTQ", "Q");
        currencySymbol.put("Rand sudafricano - ZAR", "R");
        currencySymbol.put("Real brasileño - BRL", "R$");
        currencySymbol.put("Rial qatarí - QAR", "﷼");
        currencySymbol.put("Rial saudí - SAR", "﷼");
        currencySymbol.put("Ringgit malayo - MYR", "RM");
        currencySymbol.put("Rublo ruso - RUB", "₽");
        currencySymbol.put("Rupia india - INR", "₹");
        currencySymbol.put("Guaraní Paraguay - PYG", "₲");
        currencySymbol.put("Rupia indonesia - IDR", "Rp");
        currencySymbol.put("Rupia mauriciana - MUR", "₨");
        currencySymbol.put("Taka - BDT", "৳");
        currencySymbol.put("Won surcoreano - KRW", "₩");
        currencySymbol.put("Yen - JPY", "¥");
        currencySymbol.put("Yuan chino - CNY", "¥");
        currencySymbol.put("Zloty polaco - PLN", "zł");
    }

    public static String getCurrencySymbol(String currencyCode) {
        return currencySymbol.get(currencyCode);
    }

}
