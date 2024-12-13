package edu.upc.epsevg.prop.hex;

import java.awt.Point;

/**
 *
 * @author paura
 */
public class MyStatus extends HexGameStatus{
    int hash;
    
    public MyStatus(HexGameStatus hgs){
        super(hgs);
    }
    
    public MyStatus(MyStatus hgs){
        super(hgs);
        this.hash = hgs.hash;
    }
    
    @Override
    public void placeStone(Point point) {
        super.placeStone(point);
    }
}
