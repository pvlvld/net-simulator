/*
NET-Simulator -- Network simulator.
Copyright (C) 2006 Maxim Tereshin <maxim-tereshin@yandex.ru>

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.
            
This program is distributed in the hope that it will be useful, but 
WITHOUT ANY WARRANTY; without even the implied warranty of 
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
General Public License for more details.
            
You should have received a copy of the GNU General Public License along 
with this program; if not, write to the Free Software Foundation, 
Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA 
*/

package org.netsimulator.gui;


import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;

public abstract class NetworkLink 
        extends Line2D
        implements MouseListener, MouseMotionListener
{
    protected boolean isSelected;
    protected boolean isHilighted;
    protected Color SELECTED_COLOR = Color.GREEN;
    protected Color HILIGHTED_COLOR = Color.BLUE;
    protected Color COLOR = Color.BLACK;
    protected int x1, x2, y1, y2;
    protected int gap;
    protected int id;
        
    protected static NetworkLink selectedLink; 

    
    public static NetworkLink getSelectedLink()
    {
        return selectedLink; 
    }

    public static void setSelectedLink(NetworkLink selectedLink)
    {
        NetworkLink.selectedLink = selectedLink;
    }
    
    
    
    public int getId()
    {
        return id;
    }
    
    

    public boolean isHilighted()
    {
        return isHilighted;
    }


    public boolean isSelected()
    {
        return isSelected;
    }


    public void setSelected(boolean flag)
    {
        isSelected = flag;
    }


    public void setHilighted(boolean flag)
    {
        isHilighted = flag;
    }

    public double getX1()
    {
        return x1;
    }

    public double getX2()
    {
        return x2;
    }

    public double getY1()
    {
        return y1;
    }
    
    
    public double getY2()
    {
        return y2;
    }

    
    public void setLine(double X1, double Y1, double X2, double Y2)
    {
        this.x1 = (int)X1;
        this.y1 = (int)Y1;
        this.x2 = (int)X2;
        this.y2 = (int)Y2;
    }
    

    public void setLine(int X1, int Y1, int X2, int Y2)
    {
        this.x1 = X1;
        this.y1 = Y1;
        this.x2 = X2;
        this.y2 = Y2;
    }

    
    public Rectangle2D getBounds2D()
    {
        return new Rectangle(x1, y1, x2, y2);
    }
    
    
    public Point2D getP1()
    {
        return new Point(x1, y1);
    }
    

    public Point2D getP2()
    {
        return new Point(x2, y2);
    }

    
    public void setP1(int x, int y)
    {
        this.x1 = x;
        this.y1 = y;
    }
    

    public void setP2(int x, int y)
    {
        this.x2 = x;
        this.y2 = y;
    }
    
    
    public boolean contains(int x, int y)
    {
        //System.out.println("CONTAINS: distance = "+ptLineDist(x, y));
        return ptLineDist(x, y) <= gap &&
               ((x1 <= x2 && x >= x1-gap && x <= x2+gap) ||
                (x1 >= x2 && x <= x1+gap && x >= x2-gap)) &&
               ((y1 <= y2 && y >= y1-gap && y <= y2+gap) ||
                (y1 >= y2 && y <= y1+gap && y >= y2-gap)) ; 
    }
    
    
    public abstract void show(Graphics2D g2d);
    public abstract org.netsimulator.net.Media getMedia();
    
    public void mouseClicked(MouseEvent e){}
    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    public void mouseDragged(MouseEvent e){}
    public void mouseMoved(MouseEvent e){}
}
