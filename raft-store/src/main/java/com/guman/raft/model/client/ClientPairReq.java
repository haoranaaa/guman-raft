package com.guman.raft.model.client;

import lombok.Data;

/**
 * @author duanhaoran
 * @since 2020/3/28 7:38 PM
 */
@Data
public class ClientPairReq {

    public static int PUT = 0;
    public static int GET = 1;

    int type;

    String key;

    String value;

    public ClientPairReq(Builder builder) {
        setType(builder.type);
        setKey(builder.key);
        setValue(builder.value);
    }

    public static Builder builder() {
        return new Builder();
    }

    public enum Type {
        PUT(0), GET(1);
        int code;

        Type(int code) {
            this.code = code;
        }

        public static Type value(int code ) {
            for (Type type : values()) {
                if (type.code == code) {
                    return type;
                }
            }
            return null;
        }
    }

    public static final class Builder {

        private int type;
        private String key;
        private String value;

        private Builder() {
        }


        public Builder type(int val) {
            type = val;
            return this;
        }

        public Builder key(String val) {
            key = val;
            return this;
        }

        public Builder value(String val) {
            value = val;
            return this;
        }

        public ClientPairReq build() {
            return new ClientPairReq(this);
        }
    }


}
