package br.teatro.model;

import br.teatro.view.Main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Consumidor implements Runnable{
    @Override
    public void run() {
        int i =0;
        while(true){
            if(!Main.bufferLog.isEmpty() && i < Main.bufferLog.size()){
                try {
                    FileWriter fw = new FileWriter(Main.log, true);
                    BufferedWriter bw = new BufferedWriter(fw);
                    System.out.println(Main.bufferLog.get(i));
                    bw.write(Main.bufferLog.get(i));
                    bw.newLine();

                    bw.close();
                    fw.close();
                    i++;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
