package mapproxy.server;

import java.net.*;
import java.io.*;
import java.nio.channels.*;
import java.util.concurrent.ConcurrentHashMap;
import mapproxy.core.grid.*;
import mapproxy.core.cache.*;


//******************************************************************************
//**  Main Application
//******************************************************************************
/**
 *   A lightwieght, multi-threaded web server originally designed to test 
 *   HTTP requests and responses used in the javaxt.http package.
 *
 *   Adapted from Java Network Programming, 2nd Edition:
 *   http://www.oreilly.com/catalog/javanp2/chapter/ch11.html#71137
 *
 *   Updated to use non-blocking sockets and IO streams.
 *
 ******************************************************************************/


public class HttpServer extends Thread {
    
    private int numThreads = 250;
    private int port;
    

  //**************************************************************************
  //** Constructor
  //**************************************************************************
  /**  Creates a new instance of this class. This method is called from the
   *   main method.
   *
   *  @param port The port to use for the server
   *
   */
    
    public HttpServer(int port, int numThreads) throws IOException {
        //this.server = new ServerSocket(port);
        this.port = port;
        this.numThreads = numThreads;
    }   

    
    
  //**************************************************************************
  //** Run
  //**************************************************************************
  /**  Used to start the web server (creates a thread pool and instantiates a 
   *   socket listener). All inbound requests (socket connections) will be 
   *   processed by the RequestProcessor class.
   */    
    public void run() {

      //Create Thread Pool
        for (int i = 0; i < numThreads; i++) {
             Thread t = new Thread(new RequestProcessor());
             t.start();   
        }

        /*
        System.out.println("Accepting connections on port " + port);
        System.out.println();
        */

        System.out.println("The following layers are now available on port " + port + ":");
        mapproxy.core.Layer[] layers = ((mapproxy.core.Service) mapproxy.config.Config.base_config().get("mapproxy").toObject()).getLayers();
        for (mapproxy.core.Layer layer : layers){
            System.out.println(" - " + layer.getName());
        }

        
        java.nio.channels.Selector selector = null;
        ServerSocketChannel server = null;
        try {

          //Create the selector
            selector = java.nio.channels.Selector.open();

          //Create a non-Blocking Server Socket Channel
            server = ServerSocketChannel.open();
            server.configureBlocking(false);
            server.socket().bind(new InetSocketAddress(port));
            server.register(selector, server.validOps());

        }
        catch (IOException e) {
            e.printStackTrace();
            return;
        }


      //Pass Inbound Request to RequestProcessor
        while (true) {

            try {
                selector.select();
            
                java.util.Set readyKeys = selector.selectedKeys();
                java.util.Iterator it = readyKeys.iterator();


              //Process each key at a time
                while (it.hasNext()) {

                  //Get the selection key
                    SelectionKey key = (SelectionKey)it.next();

                  //Remove it from the list to indicate that it is being processed
                    it.remove();

                    try {
                        if (key.isAcceptable()) {
                            SocketChannel request = server.accept();
                            RequestProcessor.processRequest(request);
                        }
                        else{
                            //System.err.println(key.toString());
                        }
                    }
                    catch (IOException e) {
                        key.cancel();
                    }

                }
            }
            catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }

    }
}