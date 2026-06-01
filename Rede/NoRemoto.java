package Rede;

import java.util.Objects;

public class NoRemoto {
    public final String ip;
    public final int port;

    public NoRemoto(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NoRemoto)) return false;
        NoRemoto other = (NoRemoto) o;
        return port == other.port && ip.equals(other.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port);
    }

    @Override
    public String toString() {
        return ip + ":" + port;
    }
}