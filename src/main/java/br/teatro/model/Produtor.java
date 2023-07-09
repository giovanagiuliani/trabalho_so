package br.teatro.model;

import br.teatro.view.Main;

public class Produtor implements Runnable {
    private String vaiBuffer;
    public Produtor(String vaiBuffer) {
        this.vaiBuffer = vaiBuffer;
    }

    @Override
    public void run() {
        synchronized (Main.poltronas){
            Main.bufferLog.add(vaiBuffer);
        }
    }
}
