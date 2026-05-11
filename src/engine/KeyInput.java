package engine;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyInput implements KeyListener {
    public static final int KEY_COUNT = 256;

    private boolean keys[];
    private boolean lastKeys[];

    public KeyInput() {
        keys = new boolean[KEY_COUNT];
        lastKeys = new boolean[KEY_COUNT];
    }

    public synchronized void update() {
        for ( int i = 0; i < KEY_COUNT; ++i ) {
            lastKeys[i] = keys[i];
        }
    }

    public synchronized boolean keyHit( int keyCode ) {
        return keys[keyCode] && !lastKeys[keyCode];
    }

    public synchronized boolean keyDown( int keyCode ) {
        // KeyEvent.VK_SPACE]
        return keys[keyCode];
    }

    public void keyPressed( KeyEvent e ) {
        int keyCode = e.getKeyCode();
        if ( keyCode >= 0 && keyCode < KEY_COUNT ) {
            keys[keyCode] = true;
        }
    }

    public synchronized void keyReleased( KeyEvent e ) {
        int keyCode = e.getKeyCode();
        if ( keyCode >= 0 && keyCode < KEY_COUNT ) {
            keys[keyCode] = false;
        }
    }

    public void keyTyped( KeyEvent e ) {
// no need for this
    }
}
