package Neigbours;

import ij.measure.Calibration;

public class coordinates3D extends coordinates{



        double[] z,z2,z_cal,z2_cal;

        coordinates3D(double[] x, double[] y, double[] z, Calibration c){

            super(x,y,c);
            this.z = z;
            this.z_cal = new double[this.z.length];
            for(int i=0;i<this.z.length;i++){
                this.z_cal[i] = this.z[i]*c.pixelDepth;
            }
            addZdist(z,z);


        }
        coordinates3D(Object[] x, Object[] y, Object[] z, Calibration c){
            this(Doubletodouble(x),Doubletodouble(y),Doubletodouble(z),c);
        }

        coordinates3D(double[] x1, double[] y1,double[] z1, double[] x2, double[] y2, double[] z2, Calibration c1, Calibration c2){

            super(x1,y1,x2,y2,c1,c2);
            this.z = z1;
            this.z2 = z2;
            this.z_cal = new double[this.z.length];
            for(int i=0;i<this.z.length;i++){
                this.z_cal[i] = this.z[i]*c1.pixelDepth;
            }
            this.z2_cal = new double[this.z2.length];
            for(int i=0;i<this.z2.length;i++){
                this.z2_cal[i] = this.z[i]*c2.pixelDepth;
            }
            addZdist(z1,z2);

        }
        coordinates3D(Object[] x1, Object[] y1,Object[] z1, Object[] x2, Object[] y2, Object[] z2, Calibration c1, Calibration c2){

            this(Doubletodouble(x1),Doubletodouble(y1),Doubletodouble(z1),Doubletodouble(x2),Doubletodouble(y2),Doubletodouble(z2),c1,c2);
        }

        private void addZdist(double[] z1, double[] z2 ){

            super.max=0;
            super.max_cal=0;

            for(int i=0;i<super.distmat.length;i++){
                for(int j=0;j<super.distmat[0].length;j++){
                    super.distmat[i][j] = Math.sqrt( Math.pow(super.distmat[i][j],2) + Math.pow(z1[i]-z2[j],2) );
                    super.distmat_cal[i][j] = Math.sqrt( Math.pow(super.distmat_cal[i][j],2) + Math.pow(z1[i]-z2[j],2) );
                    super.max = Math.max(super.distmat[i][j],super.max);
                    super.max_cal = Math.max(super.distmat_cal[i][j],super.max_cal);
                }
            }
        }

    public double[] getZ(boolean cal){
        if(!cal){
            return this.z;
        }
        else{
            return this.z_cal;
        }
    }

    public double[] getZ2(boolean cal){
        if(!cal){
            return this.z2;
        }
        else{
            return this.z2_cal;
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
