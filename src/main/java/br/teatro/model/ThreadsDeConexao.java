package br.teatro.model;

import br.teatro.view.Main;

import javax.print.attribute.HashPrintRequestAttributeSet;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ThreadsDeConexao implements Runnable
{
    private Socket socket;
    private final int BUFFER_SIZE = 1024;

    private int sinal = -1;
    public ThreadsDeConexao(Socket socket) {
        this.socket = socket;
    }

    public ThreadsDeConexao() {
    }

    @Override
    public void run(){
        try{
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            //Recebe a requisição
            byte[] buffer = new byte[BUFFER_SIZE];
            int nBytes = in.read(buffer);
            String str = new String(buffer, 0, BUFFER_SIZE);

            String[] linhas = str.split("\n");
            int i = 1;

            String[] linha1 = linhas[0].split(" ");
            String recurso = linha1[1];
            
            int id = 0;
            String sonome = new String();
            

            String header = "HTTP/1.1 200 OK\n" +
                    "Content-Type: ";
            if(recurso.contains("reservar")){
                String[] separaInformacoes = recurso.split("\\?");
                String[] nomeECodLugar = separaInformacoes[1].split("&");
                String nome = nomeECodLugar[0];
                String[] separanome = nome.split("=");
                sonome = separanome[1];
                id = Integer.parseInt(nomeECodLugar[1].replace("codLugar=", ""));
                efetuarReserva(recurso, id, nome);
                recurso = "/";
            }
            if(recurso.endsWith(".css")){
                header += "text/css;";
            }
            else if(recurso.endsWith(".js") || recurso.endsWith(".download")){
                header += "text/javascript;";
            }
            else if(recurso.endsWith(".jpg") || recurso.endsWith(".jpeg")){
                header += "image/jpeg;";
            }
            else if(recurso.endsWith(".png")){
                header += "image/png;";
            }
            else if(recurso.contains(".html")){
                header += "text/html;";
            }
            header += " charset=utf-8\n\n";

            if (recurso.equals("/")) {
                recurso = "index.html";
            }
            recurso = recurso.replace('/', File.separatorChar);
            int idLugar =-1;
            if(recurso.contains("?")){
                String[] separaIdLugar = recurso.split("codLugar=");
                idLugar = Integer.parseInt(separaIdLugar[1]);
                String[] recursoNovo = recurso.split(".html");
                recurso = recursoNovo[0];
                recurso += ".html";
            }
            File f = new File("/home/app/trabalho-so/arquivos_html/" + recurso);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            if(!f.exists()){
                out.write("404 NOT FOUND\n\n".getBytes(StandardCharsets.UTF_8));
            }
            else{
                InputStream fileIn = new FileInputStream("/home/app/trabalho-so/arquivos_html/" + recurso);
                //escreve arquivos
                bout.write(header.getBytes(StandardCharsets.UTF_8));
                nBytes = fileIn.read(buffer);

                do {
                    if (nBytes > 0) {
                        bout.write(buffer, 0, nBytes);
                        nBytes = fileIn.read(buffer);
                    }
                } while (nBytes == BUFFER_SIZE);
                if(nBytes > 0){
                    bout.write(buffer, 0, nBytes);
                }
            }
            String saida = Main.processaLugares(bout, idLugar);
            saida = apresentaModal(saida, sinal, id, sonome);
            out.write(saida.getBytes(StandardCharsets.UTF_8));

            out.flush();
            out.close();
            socket.close();
        }catch (IOException e){
            System.out.println(e);
        }

    }

    public void efetuarReserva(String recurso, int idLugar, String nome){
        nome = nome.replace("nome=", "");
        nome = nome.replace("+", " ");
        synchronized (Main.poltronas){
            if(!Main.poltronas.get(idLugar).isReservado()){
                Main.poltronas.get(idLugar).setReservado(true);
                Main.poltronas.get(idLugar).setNomeLugar(nome);
                String tempo = LocalDateTime.now().toString();
                new Thread(new Produtor("Endereço de IP: "+socket.getInetAddress()
                        +"\tLugar Reservado: "+ idLugar + "\tNome: "
                        + Main.poltronas.get(idLugar).getNomeLugar() + "\tHora: " + tempo)).start();
                this.sinal = 1;
            }else{
                this.sinal = 0;
            }
        }

    }
    /**
     * Recebe a string da página index e compara se o lugar já foi selecionado, e após, muda o estilo
     * */
    public String mudaEstilo(String str){
        for (Poltrona poltrona : Main.poltronas) {
            if(str.contains("<div class='col-md-1'><a role='button' " +
                    "class='btn btn-success' href='./request.html?codLugar="
                    +poltrona.getCodLugar()+"'>") && poltrona.isReservado()){
                str = str.replace("<div class='col-md-1'><a role='button' " +
                        "class='btn btn-success' href='./request.html?codLugar="
                        +poltrona.getCodLugar()+"'>", "<div class='col-md-1'><a role='button' " +
                        "class='btn btn-danger' href='./request.html?codLugar="
                        +poltrona.getCodLugar()+"'>");
            }
        }
        return str;
    }

    public String apresentaModal(String str, int sinal, int id, String nome){
        String titulo;
        String descricao;
        if(sinal==1){
            titulo = "Sucesso!";
            descricao = "Parabéns senhor(a) "+nome+"!<br>Você reservou o assento "+id+" com sucesso<br>Esperamos você aqui!";
        }
        else if(sinal==0){
            titulo = "Sentimos muito...";
            descricao = "Senhor(a)!<br>O lugar já foi reservado!";
        }
        else{
            return str;
        }
        str = str.replace("var sixnine", "meuModal.show()");
        str = str.replace("modaltitle", titulo);
        str = str.replace("modaldesc", descricao);
        return str;
    }


}
