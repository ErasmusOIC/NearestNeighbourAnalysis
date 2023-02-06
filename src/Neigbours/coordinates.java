package Neigbours;

import ij.IJ;
import ij.measure.Calibration;

public class coordinates {

    double[] x,y,x2,y2,x_cal,y_cal,x2_cal,y2_cal;

    double max,max_cal;
    double[][] distmat,distmat_cal;
    String unit;

    coordinates(double[] x, double[] y, Calibration c){

        this.x = x;
        this.y = y;
        this.x_cal = new double[this.x.length];
        this.y_cal = new double[this.y.length];
        for(int i=0;i<this.x.length;i++){
            this.x_cal[i] = this.x[i]*c.pixelWidth;
        }
        for(int i=0;i<this.y.length;i++){
            this.y_cal[i] = this.y[i]*c.pixelHeight;
        }

        this.unit = c.getUnit();

        MakeDistmat(x,y,x,y,x_cal,y_cal,x_cal,y_cal);

    }
    coordinates(Object[] x,Object[] y, Calibration c){
        this(Doubletodouble(x),Doubletodouble(y),c);
    }

    coordinates(double[] x1, double[] y1,double[] x2, double[] y2, Calibration c1, Calibration c2){

        this.x = x1;
        this.y = y1;
        this.x_cal = new double[this.x.length];
        this.y_cal = new double[this.y.length];
        this.x2 = x2;
        this.y2 = y2;
        this.x2_cal = new double[this.x2.length];
        this.y2_cal = new double[this.y2.length];
        for(int i=0;i<this.x.length;i++){
            this.x_cal[i] = this.x[i]*c1.pixelWidth;
        }
        for(int i=0;i<this.y.length;i++){
            this.y_cal[i] = this.y[i]*c1.pixelHeight;
        }
        for(int i=0;i<this.x2.length;i++){
            this.x2_cal[i] = this.x2[i]*c2.pixelWidth;
        }
        for(int i=0;i<this.y2.length;i++){
            this.y2_cal[i] = this.y2[i]*c2.pixelHeight;
        }

        this.unit = c1.getUnit();

        if(!c1.getUnit().equals(c2.getUnit())){
            IJ.log("Units of the two images are not equal, unit of the first dataset <"+c1.getUnit()+"> is shown in the table");
            IJ.log("The unit of the second dataset is <"+c2.getUnit()+">");
        }

        MakeDistmat(x1,y1,x2,y2,x_cal,y_cal,x2_cal,y2_cal);

    }
    coordinates(Object[] x1, Object[] y1,Object[] x2, Object[] y2, Calibration c1, Calibration c2){
        this(Doubletodouble(x1),Doubletodouble(y1),Doubletodouble(x2),Doubletodouble(y2),c1,c2);

    }



    private void MakeDistmat(double[] x1, double[] y1, double[] x2, double[] y2,double[] x1_cal, double[] y1_cal, double[] x2_cal, double[] y2_cal){

        double[][] dmat = new double[x1.length][x2.length];
        double[][] dmat_cal = new double[x1.length][x2.length];
        this.max = 0;
        this.max_cal = 0;

        for(int i=0;i<x1.length;i++){
            for(int j=0;j<x2.length;j++){
                dmat[i][j] = Math.sqrt( Math.pow(x1[i]-x2[j],2) + Math.pow(y1[i]-y2[j],2) );
                dmat_cal[i][j] = Math.sqrt( Math.pow(x1_cal[i]-x2_cal[j],2) + Math.pow(y1_cal[i]-y2_cal[j],2) );

                this.max = Math.max(dmat[i][j],this.max);
                this.max_cal = Math.max(dmat_cal[i][j],this.max_cal);
            }
        }

        this.distmat = dmat;
        this.distmat_cal = dmat_cal;
    }

    public double[] getX(boolean cal){
        if(!cal){
            return this.x;
        }
        else{
            return this.x_cal;
        }
    }
    public double[] getX2(boolean cal){
        if(!cal){
            return this.x2;
        }
        else{
            return this.x2_cal;
        }
    }
    public double[] getY(boolean cal){
        if(!cal){
            return this.y;
        }
        else{
            return this.y_cal;
        }
    }
    public double[] getY2(boolean cal){
        if(!cal){
            return this.y2;
        }
        else{
            return this.y2_cal;
        }
    }
    public double[][] getDistMat(boolean cal){
        if(!cal){
            return this.distmat;
        }
        else{
            return this.distmat_cal;
        }
    }
    public static double[] Doubletodouble(Object[] inp){
        double[] out = new double[inp.length];
        for(int i=0;i<inp.length;i++){
            out[i] = (double) inp[i];
        }
        return out;
    }



}


