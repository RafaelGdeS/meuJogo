package Util;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import Jogo.User;

import java.io.*;
import java.util.List;

public class XMLDoc {
    private Document documento;
    private String caminhoFicheiro;

    public XMLDoc() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            this.documento = builder.newDocument();
        } catch (ParserConfigurationException e) {
            System.out.println("ERRO ao criar documento XML: " + e.getMessage());
        }
    }

    public XMLDoc(String caminho) {
        this.caminhoFicheiro = caminho;
        carregarDoFicheiro(caminho);
    }

    public boolean carregarDoFicheiro(String caminho) {
        try {
            File arquivo = new File(caminho);
            if (!arquivo.exists()) {
                System.out.println("ERRO: Ficheiro XML não encontrado: " + caminho);
                return false;
            }

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            this.documento = builder.parse(arquivo);
            this.caminhoFicheiro = caminho;

            System.out.println("XML carregado: " + caminho);
            return true;
        } catch (IOException | SAXException | ParserConfigurationException e) {
            System.out.println("ERRO ao carregar XML: " + e.getMessage());
            return false;
        }
    }

    public boolean guardarEmFicheiro(String caminho) {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            DOMSource source = new DOMSource(documento);
            StreamResult result = new StreamResult(new File(caminho));
            transformer.transform(source, result);

            this.caminhoFicheiro = caminho;
            System.out.println("XML guardado: " + caminho);
            return true;
        } catch (TransformerException e) {
            System.out.println("ERRO ao guardar XML: " + e.getMessage());
            return false;
        }
    }

    public Element criarRaiz(String nomeRaiz) {
        Element raiz = documento.createElement(nomeRaiz);
        documento.appendChild(raiz);
        return raiz;
    }

    public Element obterRaiz() {
        return documento.getDocumentElement();
    }

    public Element criarElemento(String nomeElemento) {
        return documento.createElement(nomeElemento);
    }

    public Element criarElemento(String nomeElemento, String texto) {
        Element elemento = documento.createElement(nomeElemento);
        elemento.setTextContent(texto);
        return elemento;
    }

    public Element criarElementoComAtributo(String nomeElemento, String nomeAtributo, String valorAtributo) {
        Element elemento = documento.createElement(nomeElemento);
        elemento.setAttribute(nomeAtributo, valorAtributo);
        return elemento;
    }

    public void adicionarAtributo(Element elemento, String nomeAtributo, String valorAtributo) {
        elemento.setAttribute(nomeAtributo, valorAtributo);
    }

    public String obterAtributo(Element elemento, String nomeAtributo) {
        return elemento.getAttribute(nomeAtributo);
    }

    public NodeList obterElementosPorNome(String nomeElemento) {
        return documento.getElementsByTagName(nomeElemento);
    }

    public Element obterPrimeiroElemento(String nomeElemento) {
        NodeList lista = documento.getElementsByTagName(nomeElemento);
        if (lista.getLength() > 0) {
            return (Element) lista.item(0);
        }
        return null;
    }

    public String obterTexto(Element elemento) {
        return elemento.getTextContent();
    }

    public void definirTexto(Element elemento, String texto) {
        elemento.setTextContent(texto);
    }

    public Document obterDocumento() {
        return documento;
    }

    public void imprimir() {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            DOMSource source = new DOMSource(documento);
            StreamResult result = new StreamResult(System.out);
            transformer.transform(source, result);
        } catch (TransformerException e) {
            System.out.println("ERRO ao imprimir XML: " + e.getMessage());
        }
    }

    public String paraString() {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            DOMSource source = new DOMSource(documento);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);

            return writer.toString();
        } catch (TransformerException e) {
            System.out.println("ERRO ao converter XML para string: " + e.getMessage());
            return "";
        }
    }

    public boolean validarContraSchema(String caminhoXSD) {
        try {
            javax.xml.validation.SchemaFactory factory =
                    javax.xml.validation.SchemaFactory.newInstance(
                            javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
            javax.xml.validation.Schema schema = factory.newSchema(new File(caminhoXSD));
            javax.xml.validation.Validator validator = schema.newValidator();

            validator.validate(new javax.xml.transform.dom.DOMSource(documento));
            System.out.println("XML válido contra schema: " + caminhoXSD);
            return true;
        } catch (Exception e) {
            System.out.println("ERRO na validação: " + e.getMessage());
            return false;
        }
    }

    public static XMLDoc criarXMLEstatisticas(List<User> jogadores) {
        XMLDoc xml = new XMLDoc();
        Element raiz = xml.criarRaiz("Jogadores");

        for (User jogador : jogadores) {
            Element eleJogador = xml.criarElemento("Jogador");
            xml.adicionarAtributo(eleJogador, "nickname", jogador.nickname);

            Element nick = xml.criarElemento("Nickname", jogador.nickname);
            Element idade = xml.criarElemento("Idade", String.valueOf(jogador.age));
            Element nac = xml.criarElemento("Nacionalidade", jogador.nationality);
            Element vit = xml.criarElemento("Vitorias", String.valueOf(jogador.victories));
            Element der = xml.criarElemento("Derrotas", String.valueOf(jogador.losses));

            eleJogador.appendChild(nick);
            eleJogador.appendChild(idade);
            eleJogador.appendChild(nac);
            eleJogador.appendChild(vit);
            eleJogador.appendChild(der);

            raiz.appendChild(eleJogador);
        }

        return xml;
    }

    public static void main(String[] args) {
        XMLDoc xml = new XMLDoc();
        Element raiz = xml.criarRaiz("Jogo");

        Element config = xml.criarElemento("Configuracao");
        xml.adicionarAtributo(config, "versao", "1.0");

        Element tamanhoTab = xml.criarElemento("TamanhoTabuleiro", "3");
        Element maxJogadores = xml.criarElemento("MaxJogadores", "2");
        Element porta = xml.criarElemento("Porta", "5025");

        config.appendChild(tamanhoTab);
        config.appendChild(maxJogadores);
        config.appendChild(porta);
        raiz.appendChild(config);

        Element jogadores = xml.criarElemento("Jogadores");

        Element jog1 = xml.criarElementoComAtributo("Jogador", "id", "1");
        jog1.appendChild(xml.criarElemento("Nome", "Alice"));
        jog1.appendChild(xml.criarElemento("Vitoria", "5"));
        jogadores.appendChild(jog1);

        Element jog2 = xml.criarElementoComAtributo("Jogador", "id", "2");
        jog2.appendChild(xml.criarElemento("Nome", "Bob"));
        jog2.appendChild(xml.criarElemento("Vitoria", "3"));
        jogadores.appendChild(jog2);

        raiz.appendChild(jogadores);

        System.out.println("=== XML Criado ===");
        xml.imprimir();

        xml.guardarEmFicheiro("configuracao_jogo.xml");

        XMLDoc xmlCarregado = new XMLDoc("configuracao_jogo.xml");
        System.out.println("\n=== XML Carregado ===");
        xmlCarregado.imprimir();
    }
}