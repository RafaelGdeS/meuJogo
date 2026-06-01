package Jogo;

import Util.MyImage;
import java.util.ArrayList;
import java.util.List;

public class User {
    public final String nickname;
    public String password;
    public String nationality;
    public int age;

    public String image;
    public MyImage avatar;

    public int victories;
    public int losses;
    public int empates;

    public final List<Long> gameDurationsMs = new ArrayList<>();
    public final List<String> gameHistoryIds = new ArrayList<>();

    private long dataCriacaoConta;
    private long ultimoLogin;
    private boolean contaAtiva;

    public User(String nickname, String password, String image, String nationality, int age) {
        this.nickname = nickname;
        this.password = password;
        this.image = image;
        this.nationality = nationality;
        this.age = age;
        this.victories = 0;
        this.losses = 0;
        this.empates = 0;
        this.dataCriacaoConta = System.currentTimeMillis();
        this.ultimoLogin = System.currentTimeMillis();
        this.contaAtiva = true;
    }

    public User(String nickname, String password) {
        this(nickname, password, "", "Desconhecida", 0);
    }

    public String getNickname() { return nickname; }
    public String getPassword() { return password; }
    public String getImage() { return image; }
    public String getNationality() { return nationality; }
    public int getAge() { return age; }
    public int getVictories() { return victories; }
    public int getLosses() { return losses; }
    public int getEmpates() { return empates; }
    public long getDataCriacaoConta() { return dataCriacaoConta; }
    public long getUltimoLogin() { return ultimoLogin; }
    public boolean isContaAtiva() { return contaAtiva; }
    public List<Long> getGameDurations() { return new ArrayList<>(gameDurationsMs); }

    public void setPassword(String password) { this.password = password; }
    public void setImage(String image) { this.image = image; }
    public void setNationality(String nationality) { this.nationality = nationality; }
    public void setAge(int age) { this.age = age; }
    public void setAvatar(MyImage avatar) { this.avatar = avatar; }
    public void setContaAtiva(boolean ativa) { this.contaAtiva = ativa; }

    public void registarVitoria(long duracaoMs) {
        this.victories++;
        this.gameDurationsMs.add(duracaoMs);
        this.ultimoLogin = System.currentTimeMillis();
    }

    public void registarDerrota(long duracaoMs) {
        this.losses++;
        this.gameDurationsMs.add(duracaoMs);
        this.ultimoLogin = System.currentTimeMillis();
    }

    public void registarEmpate(long duracaoMs) {
        this.empates++;
        this.gameDurationsMs.add(duracaoMs);
        this.ultimoLogin = System.currentTimeMillis();
    }

    public double calcularTaxaVitoria() {
        int totalJogos = victories + losses + empates;
        if (totalJogos == 0) return 0.0;
        return (double) victories / totalJogos * 100;
    }

    public long calcularTempoMedioJogo() {
        if (gameDurationsMs.isEmpty()) return 0;
        long soma = gameDurationsMs.stream().mapToLong(Long::longValue).sum();
        return soma / gameDurationsMs.size();
    }

    public int getTotalJogos() {
        return victories + losses + empates;
    }

    public void atualizarUltimoLogin() {
        this.ultimoLogin = System.currentTimeMillis();
    }

    public void adicionarPartidaAoHistorico(String idPartida) {
        gameHistoryIds.add(idPartida);
    }

    public List<String> obterHistoricoPartidas() {
        return new ArrayList<>(gameHistoryIds);
    }

    public int compareTo(User outro) {
        if (this.victories != outro.victories) {
            return Integer.compare(outro.victories, this.victories);
        }
        return Integer.compare(outro.losses, this.losses);
    }

    public String getPerfilResumido() {
        return nickname + " | Idade: " + age + " | Nac: " + nationality +
               " | V: " + victories + " | D: " + losses + " | E: " + empates;
    }

    public String getPerfilDetalhado() {
        return "=== PERFIL DE " + nickname + " ===" +
               "\nIdade: " + age +
               "\nNacionalidade: " + nationality +
               "\nData de Criação: " + new java.util.Date(dataCriacaoConta) +
               "\nÚltimo Login: " + new java.util.Date(ultimoLogin) +
               "\n\nESTATÍSTICAS:" +
               "\nVitórias: " + victories +
               "\nDerrotas: " + losses +
               "\nEmpates: " + empates +
               "\nTotal de Jogos: " + getTotalJogos() +
               "\nTaxa de Vitória: " + String.format("%.2f", calcularTaxaVitoria()) + "%" +
               "\nTempo Médio de Jogo: " + calcularTempoMedioJogo() + " ms";
    }

    @Override
    public String toString() {
        return "User{" +
                "nickname='" + nickname + '\'' +
                ", age=" + age +
                ", nationality='" + nationality + '\'' +
                ", victories=" + victories +
                ", losses=" + losses +
                ", empates=" + empates +
                ", totalJogos=" + getTotalJogos() +
                '}';
    }

    public String toFileFormat() {
        StringBuilder dur = new StringBuilder();
        for (Long d : gameDurationsMs) {
            if (dur.length() > 0) dur.append(",");
            dur.append(d);
        }
        StringBuilder hist = new StringBuilder();
        for (String h : gameHistoryIds) {
            if (hist.length() > 0) hist.append(",");
            hist.append(h);
        }
        return nickname + "\t" + password + "\t" + image + "\t" + nationality + "\t" +
               age + "\t" + victories + "\t" + losses + "\t" + empates + "\t" +
               dur.toString() + "\t" + hist.toString();
    }

    public static User fromFileFormat(String linha) {
        String[] parts = linha.split("\t");
        if (parts.length < 8) return null;

        String nick = parts[0];
        String pass = parts[1];
        String img = parts[2];
        String nat = parts[3];
        int age = Integer.parseInt(parts[4]);
        int vit = Integer.parseInt(parts[5]);
        int losses = Integer.parseInt(parts[6]);
        int empates = Integer.parseInt(parts[7]);

        User u = new User(nick, pass, img, nat, age);
        u.victories = vit;
        u.losses = losses;
        u.empates = empates;

        if (parts.length >= 9 && !parts[8].isBlank()) {
            String[] durations = parts[8].split(",");
            for (String d : durations) {
                if (!d.isBlank()) u.gameDurationsMs.add(Long.parseLong(d));
            }
        }

        if (parts.length >= 10 && !parts[9].isBlank()) {
            String[] historico = parts[9].split(",");
            for (String h : historico) {
                if (!h.isBlank()) u.gameHistoryIds.add(h);
            }
        }

        return u;
    }

    public String toXML() {
        StringBuilder xml = new StringBuilder();
        xml.append("  <User nickname=\"").append(nickname).append("\">\n");
        xml.append("    <Password>").append(password).append("</Password>\n");
        xml.append("    <Image>").append(image).append("</Image>\n");
        xml.append("    <Nationality>").append(nationality).append("</Nationality>\n");
        xml.append("    <Age>").append(age).append("</Age>\n");
        xml.append("    <Victories>").append(victories).append("</Victories>\n");
        xml.append("    <Losses>").append(losses).append("</Losses>\n");
        xml.append("    <Empates>").append(empates).append("</Empates>\n");
        xml.append("  </User>\n");
        return xml.toString();
    }
}