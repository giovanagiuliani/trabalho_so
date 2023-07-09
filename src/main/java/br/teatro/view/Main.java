package br.teatro.view;

import br.teatro.model.Consumidor;
import br.teatro.model.Poltrona;
import br.teatro.model.ThreadsDeConexao;

import java.io.*;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.ArrayList;


import static java.lang.System.in;

public class Main {
    public static ArrayList<Poltrona> poltronas;
    public static String lugares = "";

    public static ThreadsDeConexao t = new ThreadsDeConexao();
    public static File log = new File("/home/app/trabalho-so/log", "log.txt");

    public static ArrayList<String> bufferLog = new ArrayList<>();
    public static void main(String[] args) throws IOException{
        ServerSocket ss = new ServerSocket(8080);
        ArrayList<Thread> conexoes = new ArrayList<>();
        poltronas = new ArrayList<>();
        Thread consumidorLog = new Thread(new Consumidor());
        consumidorLog.start();

        if(!log.exists()){
            log.createNewFile();
        }

        for(int i=0;i<50;i++){
            Poltrona p = new Poltrona(i);
            p.setReservado(false);
            poltronas.add(p);
            if(i%10==0){
                lugares += "<div class='row m-5'>\n";
            }
            lugares += "<div class='col-md-1'><a role='button' class='btn"+
                    " btn-success'" +
                    " href='./request.html?codLugar=" + p.getCodLugar() + "'>"+
                    p.getCodLugar() + "</a>"+
                    "</div>\n";
            if(i==9 || i==19 || i==29 || i==39 || i==49){
                lugares += "</div>\n";
            }
        }
        while(true) {
            //Recebe a conexão
            Socket socket = ss.accept();
            //System.out.println("Recebeu a conexão!");
            Thread r = new Thread(new ThreadsDeConexao(socket));
            r.start();
            conexoes.add(r);
        }
    }

    /**
    *Auxilia no print de lugares no index, e também altera o valor do lugar selecionado
     * na página do request.
    * */
    public static String processaLugares(ByteArrayOutputStream bout, int i) {
        String str = new String(bout.toByteArray());
        if(str.contains("{{Lugares}}")){
            str = str.replace("{{Lugares}}", Main.lugares);
            str = t.mudaEstilo(str);
        }
        if(str.contains("codlugar")){
            if(Main.poltronas.get(i).isReservado()){
                str = str.replace("reservar", "");
                str = str.replace("<input type='text' name='nome' class='form-control' id='nome'>",
                        "<input type='text' value='"+Main.poltronas.get(i).getNomeLugar()
                                +"' name='nome' disabled class='form-control' id='nome'>");

                str = str.replace("<button type='submit' class='btn btn-primary'>Reservar!</button>",
                        "<button type='submit' disabled class='btn btn-primary'>Reservar!</button>");
            }
            str = str.replace("{{codlugar}}", Integer.toString(i));
            str = str.replace("xxx", Integer.toString(i));
        }
        return str;
    }


}
