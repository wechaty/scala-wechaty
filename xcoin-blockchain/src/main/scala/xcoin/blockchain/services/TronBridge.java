package xcoin.blockchain.services;

import java.util.Locale;

public class TronBridge {
    public enum CoinType {
        //因为java中不能够把静态变量放在枚举声明之前
        USDT(1),
        TRX(2),
        ETH(3),
        BTC(4);
        private int code;

        private CoinType(int code) {
            this.code = code;
        }

        public int getCode() {
            return this.code;
        }

    }

    public enum ResourceType {
        ENERGY(1),//能量
        BANDWIDTH(2); //带宽
        private int code;

        private ResourceType(int code) {
            this.code = code;
        }

        public int getCode() {
            return this.code;
        }
    }
}
