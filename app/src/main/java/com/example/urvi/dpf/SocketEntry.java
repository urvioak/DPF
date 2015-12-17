package com.example.urvi.dpf;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * Maintain record of all the input output streams of all sockets
 * Created by Urvi on 07-Nov-15.
 */
public class SocketEntry {

    ObjectInputStream inputStream;
    ObjectOutputStream outputStream;


    public SocketEntry(ObjectInputStream is, ObjectOutputStream os){
        inputStream = is;
        outputStream = os;
    }

}
