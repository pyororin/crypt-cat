package pyororin.cryptcat.repository.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum Pair {
    BTC_JPY("btc_jpy"),
    ETC_JPY("etc_jpy"),
    LSK_JPY("lsk_jpy"),
    MONA_JPY("mona_jpy"),
    PLT_JPY("plt_jpy"),
    FNCT_JPY("fnct_jpy"),
    DAI_JPY("dai_jpy"),
    WBTC_JPY("wbtc_jpy");

    private final String value;

    public static Pair fromValue(String value) {
        return Arrays.stream(Pair.values())
                .filter(pair -> pair.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No such pair exists: " + value));
    }
}
