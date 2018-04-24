package sample;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.control.TextField;


import java.awt.*;
import java.awt.event.MouseEvent;

import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

public class Controller {
    @FXML
    private Canvas canvas;
    @FXML
    private Button drawLine;
    @FXML
    private Button drawCircle;
    @FXML
    private TextField thickness;
    @FXML
    private TextField myConsole;
    @FXML
    private Button aA;

    Point firstPoint;
    Point secondPoint;
    boolean setFirst = true;


    public void initialize() {
        canvas.setOnMouseClicked(event -> {
            if (firstPoint == null || setFirst == true) {
                firstPoint = new Point();
                firstPoint.setLocation(event.getX(), event.getY());
                myConsole.setText("first point on " + firstPoint.getLocation().toString());
                setFirst = false;
            } else {
                secondPoint = new Point();
                secondPoint.setLocation(event.getX(), event.getY());
                myConsole.setText("second point on " + secondPoint.getLocation().toString());
                setFirst = true;
            }
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.setLineWidth(1);
            gc.setFill(Color.BLACK);
            gc.fillOval(event.getX(), event.getY(), 1, 1);
        });
        drawLine.setOnMouseClicked(event -> {
            int tempThickness = Integer.parseInt(thickness.getText());
            System.out.println("first" + firstPoint.toString() + "second " + secondPoint.toString());
            SymmetricLine(firstPoint.x, firstPoint.y, secondPoint.x, secondPoint.y);
        });

        aA.setOnMouseClicked(event -> {
            ThickAntialiasedLine(firstPoint.x, firstPoint.y, secondPoint.x, secondPoint.y, 3);
        });

        drawCircle.setOnMouseClicked(event -> {
            MidpointCircle(Integer.parseInt(thickness.getText()));
        });
    }

    void putPixel(int x, int y) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setLineWidth(1);
//        gc.setFill(Color.GREEN);
        gc.fillOval(x, y, 1, 1);
    }

    void SymmetricLine(int x1, int y1, int x2, int y2) {
        int dx, dy, xi, yi;
        if (x2 > x1) {
            dx = x2 - x1;
            xi = 1;
        } else {
            dx = x1 - x2;
            xi = -1;
        }
        if (y2 > y1) {
            dy = y2 - y1;
            yi = 1;
        } else {
            dy = y1 - y2;
            yi = -1;
        }
        int xf = x1, yf = y1;
        int xb = x2, yb = y2;
        putPixel(xf, yf);
        putPixel(xb, yb);

        if (dx > dy) {
            int d = 2 * dy - dx;
            int dE = 2 * dy;
            int dNE = 2 * (dy - dx);
            while (xf != xb && xf != xb + 1) {
                xf += xi;
                xb -= xi;
                if (d < 0)
                    d += dE;
                else {
                    d += dNE;
                    yf += yi;
                    yb -= yi;
                }
                putPixel(xf, yf);
                putPixel(xb, yb);
                for (int i = 1; i < Integer.parseInt(thickness.getText()); i++) {
                    putPixel(xf, yf + i);
                    putPixel(xb, yb + i);
                    putPixel(xf, yf - i);
                    putPixel(xb, yb- i);
                }
            }
        } else {
            int d = 2 * dx - dy;
            int dE = dx * 2;
            int dNE = 2 * (dx - dy);
            while (yf != yb && yf != yb + 1) {
                yf += yi;
                yb -= yi;
                if (d < 0)
                    d += dE;
                else {
                    d += dNE;
                    xf += xi;
                    xb -= xi;
                }
                putPixel(xf, yf);
                putPixel(xb, yb);
                for (int i = 1; i < Integer.parseInt(thickness.getText()); i++) {
                    putPixel(xf+i, yf);
                    putPixel(xb+i, yb);
                    putPixel(xf-i, yf);
                    putPixel(xb-i, yb);
                }
            }
        }
    }

    void MidpointCircle(int R) {
        int d = 1 - R;
        int x = 0;
        int y = R;
        putPixel(x, y);
        while (y > x) {
            if (d < 0) //move to E
                d += 2 * x + 3;
            else
            {
                d += 2 * x - 2 * y + 5;
                --y;
            }
            ++x;
            putPixel(secondPoint.x + x, secondPoint.y + y);
            putPixel(secondPoint.x + x, secondPoint.y - y);
            putPixel(secondPoint.x + y, secondPoint.y + x);
            putPixel(secondPoint.x + y, secondPoint.y - x);
            putPixel(secondPoint.x - x, secondPoint.y + y);
            putPixel(secondPoint.x - x, secondPoint.y - y);
            putPixel(secondPoint.x - y, secondPoint.y + x);
            putPixel(secondPoint.x - y, secondPoint.y - x);
        }
    }

    void ThickAntialiasedLine(int x1, int y1, int x2, int y2, float thickness) {
//initial values in Bresenham;s algorithm
        int dx = x2 - x1, dy = y2 - y1;
        int dE = 2 * dy, dNE = 2 * (dy - dx);
        int d = 2 * dy - dx;
        int two_v_dx = 0; //numerator, v=0 for the first pixel
        double invDenom = 1 / (2 * sqrt(dx * dx + dy * dy)); //inverted denominator
        double two_dx_invDenom = 2 * dx * invDenom; //precomputed constant
        int x = x1, y = y1;
        int i;
        IntensifyPixel(x, y, thickness, 0);
        for (i = 1; IntensifyPixel(x, y + i, thickness, i * two_dx_invDenom) > 0; ++i) ;
        for (i = 1; IntensifyPixel(x, y - i, thickness, i * two_dx_invDenom) > 0; ++i) ;
        while (x < x2) {
            x++;
            if (d < 0) // move to E
            {
                two_v_dx = d + dx;
                d += dE;
            } else // move to NE
            {
                two_v_dx = d - dx;
                d += dNE;
                ++y;
            }
// Now set the chosen pixel and its neighbors
            IntensifyPixel(x, y, thickness, two_v_dx * invDenom);
            for (i = 1; IntensifyPixel(x, y + i, thickness, i * two_dx_invDenom - two_v_dx * invDenom) > 0; ++i) ;
            for (i = 1; IntensifyPixel(x, y - i, thickness, i * two_dx_invDenom + two_v_dx * invDenom) > 0; ++i) ;
        }
    }



    double IntensifyPixel(int x, int y, double thickness, double distance)
    {
        double r = 0.5f;
        double cov = coverage(thickness, distance, r);
        if ( cov > 0 ) {
            GraphicsContext gc = canvas.getGraphicsContext2D();
            gc.setFill(new Color((Color.BLACK.getRed()*cov), (Color.BLACK.getGreen()*cov), (Color.BLACK.getBlue()*cov),1));
            putPixel(x, y);
        }
        else{
//            gc.setFill(Color.GREEN);
        }
        return cov;
    }

    double coverage(double thickness, double distance, double r){
        double c;
        if (thickness <= distance){
            c = cov(distance - thickness/2, 0.5);
        }
        else {
            c = 1 - cov(thickness/2 - distance, 0.5);
        }
        return c;
    }

    double cov(double d, double r){
        if(d<= r){
            return ((1/Math.PI) * (Math.acos(d/r)) - ((d/(Math.PI * r * r))*sqrt(r * r * d * d)));
        }
        return 0;
    }
}
