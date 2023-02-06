package myClasses;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;

public class _3dPoint_test implements PlugIn {

    public void run(String arg){
        Point3D[] pt;
        ImagePlus imp = WindowManager.getCurrentImage();
        try {
            pt = new getPointRois(imp).getRois();
        }catch(IndexOutOfBoundsException e){
            IJ.showMessage("no points selected");
            return;
        }
        int x,y,z;


        for(int i=0; i<pt.length;i++){
            x = (int) pt[i].getX();
            y = (int) pt[i].getY();
            z = (int) pt[i].getZ();

            IJ.log(" "+x+" "+y+" "+z);
        }
    }
}
