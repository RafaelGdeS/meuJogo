package Jogo;

import java.io.*;
import java.util.*;

public class UserStore {
    private final File dbFile;
    private final Map<String, User> users = new HashMap<>();

    public UserStore(String filename) throws IOException {
        this.dbFile = new File(filename);
        if (!dbFile.exists()) {
            dbFile.createNewFile();
        } else {
            load();
        }
    }

    public User register(String nickname, String password, String image, String nationality, int age) throws IOException {
        if (users.containsKey(nickname)) return null;
        User u = new User(nickname, password, image, nationality, age);
        users.put(nickname, u);
        save();
        return u;
    }

    public User login(String nickname, String password) {
        User u = users.get(nickname);
        if (u != null && u.password.equals(password)) return u;
        return null;
    }

    public boolean updateImage(String nickname, String image) throws IOException {
        User u = users.get(nickname);
        if (u == null) return false;
        u.image = image;
        save();
        return true;
    }

    public void recordGameResult(String nickname, boolean venceu, long duracaoMs) throws IOException {
        User u = users.get(nickname);
        if (u == null) return;
        if (venceu) u.victories++;
        else u.losses++;
        u.gameDurationsMs.add(duracaoMs);
        save();
    }

    public String profileLine(String nickname) {
        User u = users.get(nickname);
        if (u == null) return "Utilizador não encontrado.";
        return "Perfil: " + u.nickname +
               ", Idade: " + u.age +
               ", Nacionalidade: " + u.nationality +
               ", Vitórias: " + u.victories +
               ", Derrotas: " + u.losses +
               ", Jogos: " + u.gameDurationsMs.size();
    }

    private void load() throws IOException {
        users.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(dbFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\t");
                if (parts.length < 7) continue;
                String nick = parts[0];
                String pass = parts[1];
                String img  = parts[2];
                String nat  = parts[3];
                int age     = Integer.parseInt(parts[4]);
                int vits    = Integer.parseInt(parts[5]);
                int losses  = Integer.parseInt(parts[6]);

                User u = new User(nick, pass, img, nat, age);
                u.victories = vits;
                u.losses = losses;
                if (parts.length >= 8) {
                    String[] durations = parts[7].split(",");
                    for (String d : durations) {
                        if (!d.isBlank()) u.gameDurationsMs.add(Long.parseLong(d));
                    }
                }
                users.put(nick, u);
            }
        }
    }

    private void save() throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(dbFile, false))) {
            for (User u : users.values()) {
                StringJoiner dur = new StringJoiner(",");
                for (Long ms : u.gameDurationsMs) dur.add(ms.toString());

                pw.println(u.nickname + "\t"
                        + u.password + "\t"
                        + u.image + "\t"
                        + u.nationality + "\t"
                        + u.age + "\t"
                        + u.victories + "\t"
                        + u.losses + "\t"
                        + dur.toString());
            }
        }
    }

    public List<User> allUsers() {
        return new ArrayList<>(users.values());
    }
}