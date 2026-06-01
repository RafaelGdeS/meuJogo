package Jogo;

import java.util.*;

public class Nationality {
    private final String nome;
    private final String codigoISO2;
    private final String codigoISO3;
    private final String bandeira;
    private final String continente;
    private final String codigoTelefonico;

    private static final Map<String, Nationality> nacionalidades = new HashMap<>();

    static {
        registarNacionalidade("Portugal", "PT", "PRT", "🇵🇹", "Europa", "+351");
        registarNacionalidade("Espanha", "ES", "ESP", "🇪🇸", "Europa", "+34");
        registarNacionalidade("França", "FR", "FRA", "🇫🇷", "Europa", "+33");
        registarNacionalidade("Alemanha", "DE", "DEU", "🇩🇪", "Europa", "+49");
        registarNacionalidade("Reino Unido", "GB", "GBR", "🇬🇧", "Europa", "+44");
        registarNacionalidade("Itália", "IT", "ITA", "🇮🇹", "Europa", "+39");
        registarNacionalidade("Brasil", "BR", "BRA", "🇧🇷", "América do Sul", "+55");
        registarNacionalidade("México", "MX", "MEX", "🇲🇽", "América do Norte", "+52");
        registarNacionalidade("Estados Unidos", "US", "USA", "🇺🇸", "América do Norte", "+1");
        registarNacionalidade("Canadá", "CA", "CAN", "🇨🇦", "América do Norte", "+1");
        registarNacionalidade("Japão", "JP", "JPN", "🇯🇵", "Ásia", "+81");
        registarNacionalidade("China", "CN", "CHN", "🇨🇳", "Ásia", "+86");
        registarNacionalidade("Índia", "IN", "IND", "🇮🇳", "Ásia", "+91");
        registarNacionalidade("Austrália", "AU", "AUS", "🇦🇺", "Oceânia", "+61");
        registarNacionalidade("África do Sul", "ZA", "ZAF", "🇿🇦", "África", "+27");
        registarNacionalidade("Egito", "EG", "EGY", "🇪🇬", "África", "+20");
        registarNacionalidade("Rússia", "RU", "RUS", "🇷🇺", "Europa/Ásia", "+7");
        registarNacionalidade("Turquia", "TR", "TUR", "🇹🇷", "Ásia/Europa", "+90");
        registarNacionalidade("Grécia", "GR", "GRC", "🇬🇷", "Europa", "+30");
        registarNacionalidade("Polónia", "PL", "POL", "🇵🇱", "Europa", "+48");
    }

    private Nationality(String nome, String codigoISO2, String codigoISO3, 
                       String bandeira, String continente, String codigoTelefonico) {
        this.nome = nome;
        this.codigoISO2 = codigoISO2;
        this.codigoISO3 = codigoISO3;
        this.bandeira = bandeira;
        this.continente = continente;
        this.codigoTelefonico = codigoTelefonico;
    }

    private static void registarNacionalidade(String nome, String iso2, String iso3, 
                                             String bandeira, String continente, String telefone) {
        Nationality nat = new Nationality(nome, iso2, iso3, bandeira, continente, telefone);
        nacionalidades.put(nome.toLowerCase(), nat);
        nacionalidades.put(iso2.toLowerCase(), nat);
        nacionalidades.put(iso3.toLowerCase(), nat);
    }

    public String getNome() { return nome; }
    public String getCodigoISO2() { return codigoISO2; }
    public String getCodigoISO3() { return codigoISO3; }
    public String getBandeira() { return bandeira; }
    public String getContinente() { return continente; }
    public String getCodigoTelefonico() { return codigoTelefonico; }

    public static Nationality obterPorNome(String nome) {
        if (nome == null || nome.isBlank()) return null;
        return nacionalidades.get(nome.toLowerCase());
    }

    public static Nationality obterPorCodigoISO(String codigo) {
        if (codigo == null || codigo.isBlank()) return null;
        return nacionalidades.get(codigo.toLowerCase());
    }

    public static boolean ehValida(String nome) {
        return nacionalidades.containsKey(nome.toLowerCase());
    }

    public static List<Nationality> obterTodas() {
        Set<Nationality> conjunto = new HashSet<>(nacionalidades.values());
        return new ArrayList<>(conjunto);
    }

    public static List<Nationality> obterPorContinente(String continente) {
        List<Nationality> resultado = new ArrayList<>();
        Set<Nationality> conjunto = new HashSet<>(nacionalidades.values());
        for (Nationality nat : conjunto) {
            if (nat.continente.equalsIgnoreCase(continente)) {
                resultado.add(nat);
            }
        }
        return resultado;
    }

    public static List<String> obterContinentes() {
        Set<String> continentes = new HashSet<>();
        Set<Nationality> conjunto = new HashSet<>(nacionalidades.values());
        for (Nationality nat : conjunto) {
            continentes.add(nat.continente);
        }
        return new ArrayList<>(continentes);
    }

    public static boolean validarCodigoISO2(String codigo) {
        if (codigo == null || codigo.length() != 2) return false;
        Nationality nat = nacionalidades.get(codigo.toLowerCase());
        return nat != null;
    }

    public static boolean validarCodigoISO3(String codigo) {
        if (codigo == null || codigo.length() != 3) return false;
        Nationality nat = nacionalidades.get(codigo.toLowerCase());
        return nat != null;
    }

    public int compareTo(Nationality outra) {
        return this.nome.compareTo(outra.nome);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Nationality)) return false;
        Nationality that = (Nationality) o;
        return codigoISO2.equals(that.codigoISO2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigoISO2);
    }

    public String getNomeComBandeira() {
        return bandeira + " " + nome;
    }

    public String getInfoCompleta() {
        return nome +
               " | ISO: " + codigoISO2 + "/" + codigoISO3 +
               " | Continente: " + continente +
               " | Tel: " + codigoTelefonico +
               " | " + bandeira;
    }

    @Override
    public String toString() {
        return nome + " (" + codigoISO2 + ")";
    }

    public static void main(String[] args) {
        Nationality pt = Nationality.obterPorNome("Portugal");
        System.out.println("Portugal: " + pt.getInfoCompleta());

        Nationality es = Nationality.obterPorCodigoISO("ES");
        System.out.println("Espanha: " + es.getNomeComBandeira());

        System.out.println("Portugal válida? " + Nationality.ehValida("Portugal"));
        System.out.println("Narnia válida? " + Nationality.ehValida("Narnia"));

        System.out.println("\nContinentes: " + Nationality.obterContinentes());

        System.out.println("\nNacionalidades da Europa:");
        List<Nationality> europa = Nationality.obterPorContinente("Europa");
        for (Nationality nat : europa) {
            System.out.println("  " + nat.getNomeComBandeira());
        }

        System.out.println("\nTotal de nacionalidades: " + 
                          new HashSet<>(Nationality.obterTodas()).size());
    }
}