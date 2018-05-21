package cn.zysung.miaosha.redis;

public abstract class BasePrefix implements KeyPrefix {

    private int expireSeconds;
    private String prefix;

    public BasePrefix(int expireSeconds, String prefix) {
        this.expireSeconds = expireSeconds;
        this.prefix = prefix;
    }

    public BasePrefix(String prefix) {
        this(0,prefix);
    }

    @Override
    public int expireSeconds() {
        return expireSeconds;//默认0代表永不过期
    }

    @Override
    public String getPrefix() {
        String claName= getClass().getSimpleName();
        return claName+":"+prefix;
    }
}
