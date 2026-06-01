package Util;

import java.io.*;
import java.util.Base64;

public class MyImage {
    private String caminho;
    private byte[] dados;
    private String base64;
    private String formato;
    private String descricao;

    public MyImage() {
        this.caminho = "";
        this.dados = new byte[0];
        this.base64 = "";
        this.formato = "";
        this.descricao = "";
    }

    public MyImage(String caminho) {
        this.caminho = caminho;
        this.descricao = caminho;
        carregarDoFicheiro();
    }

    public MyImage(String caminho, String descricao) {
        this.caminho = caminho;
        this.descricao = descricao;
        carregarDoFicheiro();
    }

    public boolean carregarDoFicheiro() {
        try {
            File file = new File(caminho);
            if (!file.exists()) {
                System.out.println("ERRO: Ficheiro não encontrado: " + caminho);
                return false;
            }

            String nome = file.getName();
            int ponto = nome.lastIndexOf('.');
            if (ponto > 0) {
                this.formato = nome.substring(ponto + 1).toUpperCase();
            }

            FileInputStream fis = new FileInputStream(file);
            this.dados = fis.readAllBytes();
            fis.close();

            this.base64 = Base64.getEncoder().encodeToString(this.dados);

            System.out.println("Imagem carregada: " + caminho + " (" + dados.length + " bytes)");
            return true;
        } catch (IOException e) {
            System.out.println("ERRO ao carregar imagem: " + e.getMessage());
            return false;
        }
    }

    public boolean guardarDoBase64(String base64String, String caminhoDestino) {
        try {
            byte[] decodificado = Base64.getDecoder().decode(base64String);
            FileOutputStream fos = new FileOutputStream(caminhoDestino);
            fos.write(decodificado);
            fos.close();

            this.caminho = caminhoDestino;
            this.dados = decodificado;
            this.base64 = base64String;

            System.out.println("Imagem guardada: " + caminhoDestino);
            return true;
        } catch (IOException e) {
            System.out.println("ERRO ao guardar imagem: " + e.getMessage());
            return false;
        }
    }

    public String obterBase64() {
        if (base64.isEmpty() && dados.length > 0) {
            this.base64 = Base64.getEncoder().encodeToString(this.dados);
        }
        return base64;
    }

    public byte[] obterDados() { return dados; }
    public int obterTamanho() { return dados.length; }
    public String obterFormato() { return formato; }
    public String obterDescricao() { return descricao; }
    public void definirDescricao(String descricao) { this.descricao = descricao; }
    public String obterCaminho() { return caminho; }
    public boolean estaVazia() { return dados.length == 0; }

    public MyImage clonar() {
        MyImage copia = new MyImage();
        copia.caminho = this.caminho;
        copia.dados = this.dados.clone();
        copia.base64 = this.base64;
        copia.formato = this.formato;
        copia.descricao = this.descricao;
        return copia;
    }

    public void redimensionar(double escala) {
        if (escala > 0 && escala <= 1) {
            int novoTamanho = (int) (dados.length * escala);
            byte[] novosDados = new byte[novoTamanho];
            System.arraycopy(dados, 0, novosDados, 0, novoTamanho);
            this.dados = novosDados;
            this.base64 = Base64.getEncoder().encodeToString(this.dados);
            System.out.println("Imagem redimensionada para " + escala * 100 + "%");
        }
    }

    @Override
    public String toString() {
        return "MyImage{" +
                "descricao='" + descricao + '\'' +
                ", formato='" + formato + '\'' +
                ", tamanho=" + dados.length +
                " bytes}";
    }

    public static void main(String[] args) {
        MyImage img = new MyImage();
        img.definirDescricao("Avatar do Jogador 1");

        img.dados = new byte[]{-119, 80, 78, 71, 13, 10, 26, 10};
        img.formato = "PNG";
        img.base64 = Base64.getEncoder().encodeToString(img.dados);

        System.out.println("Imagem criada: " + img);
        System.out.println("Base64: " + img.obterBase64().substring(0, Math.min(50, img.obterBase64().length())) + "...");
        System.out.println("Tamanho: " + img.obterTamanho() + " bytes");

        img.guardarDoBase64(img.obterBase64(), "output_avatar.png");
    }
}