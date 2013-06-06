package convo;


import org.lwjgl.input.Keyboard;

import visualize.FrameWork;

public abstract class BlurBasis extends FrameWork
{    
    public BlurBasis(int w, int h) 
    {
        super(w, h, false, false);
    }
    @Override
    public void processKeyPressed(int key)
    {
        super.processKeyPressed(key);
        switch(key)
        {
        case Keyboard.KEY_LEFT : decreaseStandardDevation(); break;
        case Keyboard.KEY_RIGHT : increaseStandardDevation(); break;
        case Keyboard.KEY_UP : increaseMaskSize(); break;
        case Keyboard.KEY_DOWN : decreaseMaskSize(); break;
        }
    }
    
    public abstract void increaseMaskSize();
    public abstract void decreaseMaskSize();
    
    public abstract void increaseStandardDevation();
    public abstract void decreaseStandardDevation();
    
}
