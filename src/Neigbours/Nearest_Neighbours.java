package Neigbours;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.macro.ExtensionDescriptor;
import ij.macro.Functions;
import ij.macro.MacroExtension;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.filter.Analyzer;
import myClasses.Point3D;
import myClasses.getPointRois;

import java.util.ArrayList;

public class Nearest_Neighbours implements PlugIn, MacroExtension {


    coordinates c;


    public void run(String s){
        if(s.equals("Ext")) {
            if (!IJ.macroRunning()) {
                IJ.error("Cannot install extensions from outside a macro!");
                return;
            } else {
                Functions.registerExtensions(this);
            }
        }else {

            Boolean strict, single, ThreeD;
            ArrayList<pair> pairset = null;
            ArrayList<pair> pairset_cal = null;
            double[] x, y, z;
            double[] x2 = null;
            double[] y2 = null;
            double[] z2 = null;
            Calibration c1;
            Calibration c2 = null;

            int[] idList = WindowManager.getIDList();
            String[] titles = new String[idList.length + 1];
            int ID1 = 0;
            int ID2 = 0;

            for (int i = 0; i < idList.length; i++) {
                titles[i] = WindowManager.getImage(idList[i]).getTitle();
            }

            titles[idList.length] = "none";

            //dialogbox
            GenericDialog gd = new GenericDialog("Neighbour settings");
            gd.addChoice("Image_1", titles, titles[idList.length]);
            gd.addChoice("Image_2", titles, titles[idList.length]);
            gd.addCheckbox("3D", false);
            gd.addCheckbox("single image", true);
            gd.addCheckbox("strickt pairs", false);
            gd.showDialog();

            if (gd.wasCanceled()) {
                return;
            }
            ID1 = gd.getNextChoiceIndex();
            ID2 = gd.getNextChoiceIndex();
            ThreeD = gd.getNextBoolean();
            single = gd.getNextBoolean();
            strict = gd.getNextBoolean();


            if (!single && ID1 == idList.length && ID2 == idList.length) {
                return;
            }
            if (single && ID1 == idList.length) {
                return;
            }


            ImagePlus imp = WindowManager.getImage(idList[ID1]);


            Point3D[] pta;
            try {
                pta = new getPointRois(imp).getRois();
            } catch (IndexOutOfBoundsException e) {
                ResultsTable rt = new ResultsTable();
                Analyzer.setResultsTable(rt);
                rt.show("Results");
                IJ.log("no point selection present");
                return;
            }

            x = new double[pta.length];
            y = new double[pta.length];
            z = new double[pta.length];

            for (int i = 0; i < pta.length; i++) {
                x[i] = pta[i].getX();
                y[i] = pta[i].getY();
                z[i] = pta[i].getZ();

            }

            c1 = imp.getCalibration();

            if (!single) {
                imp = WindowManager.getImage(idList[ID2]);

                try {
                    pta = new getPointRois(imp).getRois();
                } catch (IndexOutOfBoundsException e) {
                    ResultsTable rt = new ResultsTable();
                    Analyzer.setResultsTable(rt);
                    rt.show("Results");
                    IJ.log("no point selection present");
                    return;
                }

                x2 = new double[pta.length];
                y2 = new double[pta.length];
                z2 = new double[pta.length];

                for (int i = 0; i < pta.length; i++) {
                    x2[i] = pta[i].getX();
                    y2[i] = pta[i].getY();
                    z2[i] = pta[i].getZ();
                }

                c2 = imp.getCalibration();
            }

            if (single) {
                if (ThreeD) {
                    this.c = new coordinates3D(x, y, z, c1);
                } else {
                    this.c = new coordinates(x, y, c1);
                }
            } else {
                if (ThreeD) {
                    this.c = new coordinates3D(x, y, z, x2, y2, z2, c1, c2);
                } else {
                    this.c = new coordinates(x, y, x2, y2, c1, c2);
                }
            }

            if (strict) {
                if (single) {
                    pairset = getNNstrickt(c, false);
                    pairset_cal = getNNstrickt(c, true);
                } else {
                    pairset = getNNstrickt_twosets(c, false);
                    pairset_cal = getNNstrickt_twosets(c, true);
                }
            } else {
                if (single) {
                    pairset = getNNloose(c, false);
                    pairset_cal = getNNloose(c, true);
                } else {
                    pairset = getNNloose_twosets(c, false);
                    pairset_cal = getNNloose_twosets(c, true);
                }
            }


            makeResults(pairset,pairset_cal);

        }


    }


    //methods for single point set


    private ArrayList<pair> getNNstrickt(coordinates c, boolean cal){

        if(c instanceof coordinates3D){
            return getNNstrickt((coordinates3D) c,cal);
        }

        ArrayList<pair> pairs = new ArrayList<>();

        if(c.getX(cal).length<=1){

            if(c.getX(cal).length==1) {
                pairs.add(new pair(c.getX(cal)[0], c.getY(cal)[0], 0, 0, -1, -1));
            }else{
                pairs.add(new pair(0, 0, 0, 0, -1, -1));
            }

            return pairs;
        }

        boolean[] assigned = new boolean[c.getX(cal).length];
        int[] minRow,minCol;
        boolean test = false;
        int cnt;




        while(!test) {
            minRow = getMinRow(c.getDistMat(cal), assigned);
            minCol = getMinCol(c.getDistMat(cal), assigned);

            for (int i = 0; i < minRow.length; i++) {
                if (minCol[minRow[i]] == i && !assigned[i]) {
                    assigned[i] = true;
                    assigned[minRow[i]] = true;
                    pairs.add(new pair(c.getX(cal)[i],c.getY(cal)[i],c.getX(cal)[minRow[i]],c.getY(cal)[minRow[i]],i,minRow[i]));
                }
            }

            cnt = 0;

            for (boolean i:assigned) {
                if (!i) {
                    cnt++;
                }
            }

            if(cnt<=1){
                test=true;
            }

            IJ.showProgress(assigned.length-cnt,assigned.length-1);

        }

        return pairs;


    }
    private ArrayList<pair> getNNstrickt(coordinates3D c, boolean cal){


        ArrayList<pair> pairs = new ArrayList<>();

        if(c.getX(cal).length<=1){

            if(c.getX(cal).length==1) {
                pairs.add(new pair(c.getX(cal)[0], c.getY(cal)[0], c.getZ(cal)[0], 0, 0,0, -1, -1));
            }else{
                pairs.add(new pair(0, 0,0, 0, 0,0, -1, -1));
            }

            return pairs;
        }

        boolean[] assigned = new boolean[c.getX(cal).length];
        int[] minRow,minCol;
        boolean test = false;
        int cnt;




        while(!test) {
            minRow = getMinRow(c.getDistMat(cal), assigned);
            minCol = getMinCol(c.getDistMat(cal), assigned);

            for (int i = 0; i < minRow.length; i++) {
                if (minCol[minRow[i]] == i && !assigned[i]) {
                    assigned[i] = true;
                    assigned[minRow[i]] = true;
                    pairs.add(new pair(c.getX(cal)[i],c.getY(cal)[i],c.getZ(cal)[i],c.getX(cal)[minRow[i]],c.getY(cal)[minRow[i]],c.getZ(cal)[minRow[i]],i,minRow[i]));
                }
            }

            cnt = 0;

            for (boolean i:assigned) {
                if (!i) {
                    cnt++;
                }
            }

            if(cnt<=1){
                test=true;
            }

            IJ.showProgress(assigned.length-cnt,assigned.length-1);

        }

        return pairs;


    }

    private ArrayList<pair> getNNloose(coordinates c,boolean cal){

        if(c instanceof coordinates3D){
            return getNNloose((coordinates3D) c,cal);
        }

        ArrayList<pair> pairs = new ArrayList<>();

        if(c.getX(cal).length<=1){

            if(c.getX(cal).length==1) {
                pairs.add(new pair(c.getX(cal)[0], c.getY(cal)[0], 0, 0, -1, -1));
            }else{
                pairs.add(new pair(0, 0, 0, 0, -1, -1));
            }

            return pairs;
        }

        boolean[] assigned = new boolean[c.getX(cal).length];
        int[] minRow;


        minRow = getMinRow(c.getDistMat(cal), assigned);


        for (int i = 0; i < minRow.length; i++) {


                pairs.add(new pair(c.getX(cal)[i],c.getY(cal)[i],c.getX(cal)[minRow[i]],c.getY(cal)[minRow[i]],i,minRow[i]));

                IJ.showProgress(i,minRow.length-1);

        }

        return pairs;



    }
    private ArrayList<pair> getNNloose(coordinates3D c,boolean cal){


        ArrayList<pair> pairs = new ArrayList<>();

        if(c.getX(cal).length<=1){

            if(c.getX(cal).length==1) {
                pairs.add(new pair(c.getX(cal)[0], c.getY(cal)[0],c.getZ(cal)[0],0, 0, 0, -1, -1));
            }else{
                pairs.add(new pair(0, 0, 0, 0,0,0, -1, -1));
            }

            return pairs;
        }

        boolean[] assigned = new boolean[c.getX(cal).length];
        int[] minRow;


        minRow = getMinRow(c.getDistMat(cal), assigned);


        for (int i = 0; i < minRow.length; i++) {


            pairs.add(new pair(c.getX(cal)[i],c.getY(cal)[i],c.getZ(cal)[i],c.getX(cal)[minRow[i]],c.getY(cal)[minRow[i]],c.getZ(cal)[minRow[i]],i,minRow[i]));

            IJ.showProgress(i,minRow.length-1);

        }

        return pairs;



    }



    private int[] getMinRow(double[][] distmat, boolean[] assigned){

        int[] minRow = new int[distmat.length];
        double mindist;

        for(int i=0;i<distmat.length;i++){

            mindist = c.max+1;
            for(int j=0;j<distmat[i].length;j++){

                if(i!=j && !assigned[i] && !assigned[j]){
                    if(distmat[i][j]<mindist){
                        mindist = distmat[i][j];
                        minRow[i] = j;

                    }
                }
            }
        }


        return minRow;

    }
    private int[] getMinCol(double[][] distmat, boolean[] assigned){

        int[] minCol = new int[distmat[0].length];
        double mindist;

        for(int i=0;i<distmat[0].length;i++){
            mindist = c.max+1;
            for(int j=0;j<distmat.length;j++){
                if(i!=j && !assigned[i] && !assigned[j]){
                    if(distmat[j][i]<mindist){
                        mindist = distmat[i][j];
                        minCol[i] = j;
                    }
                }
            }
        }

        return minCol;

    }

    //methods for dual point set

    private ArrayList<pair> getNNloose_twosets(coordinates c, boolean cal){

        if(c instanceof coordinates3D){
            return getNNloose_twosets((coordinates3D) c,cal);
        }


        ArrayList<pair> pairs = new ArrayList<>();

        if(c.getX(cal).length < 1 || c.getX2(cal).length <1){
            pairs.add(new pair(0,0,0,0,-1,-1));
            return pairs;
        }

        boolean[] assigned_row = new boolean[c.getX(cal).length];
        boolean[] assigned_col = new boolean[c.getX2(cal).length];

        int[] minRow = getMinRow(c.getDistMat(cal), assigned_row, assigned_col);
        int[] minCol = getMinCol(c.getDistMat(cal), assigned_row, assigned_col);

        for (int i = 0; i < minRow.length; i++) {

            assigned_row[i] = true;
            assigned_col[minRow[i]] = true;
            pairs.add(new pair(c.getX(cal)[i],c.getY(cal)[i],c.getX2(cal)[minRow[i]],c.getY2(cal)[minRow[i]],i,minRow[i]));

            IJ.showProgress(i,minRow.length-1);

        }

        for (int i = 0; i < minCol.length; i++) {

            if(!assigned_col[i]) {
                assigned_col[i] = true;
                assigned_row[minCol[i]] = true;
                pairs.add(new pair(c.getX(cal)[minCol[i]], c.getY(cal)[minCol[i]],c.getX2(cal)[i], c.getY2(cal)[i],  minCol[i],i));
            }

            IJ.showProgress(i,minRow.length-1);

        }


        return pairs;



    }
    private ArrayList<pair> getNNloose_twosets(coordinates3D c, boolean cal){




        ArrayList<pair> pairs = new ArrayList<>();

        if(c.getX(cal).length < 1 || c.getX2(cal).length <1){
            pairs.add(new pair(0,0,0,0,0,0,-1,-1));
            return pairs;
        }

        boolean[] assigned_row = new boolean[c.getX(cal).length];
        boolean[] assigned_col = new boolean[c.getX2(cal).length];

        int[] minRow = getMinRow(c.getDistMat(cal), assigned_row, assigned_col);
        int[] minCol = getMinCol(c.getDistMat(cal), assigned_row, assigned_col);

        for (int i = 0; i < minRow.length; i++) {

            assigned_row[i] = true;
            assigned_col[minRow[i]] = true;
            pairs.add(new pair(c.getX(cal)[i],c.getY(cal)[i],c.getZ(cal)[i],c.getX2(cal)[minRow[i]],c.getY2(cal)[minRow[i]],c.getZ2(cal)[minRow[i]],i,minRow[i]));

            IJ.showProgress(i,minRow.length-1);

        }

        for (int i = 0; i < minCol.length; i++) {

            if(!assigned_col[i]) {
                assigned_col[i] = true;
                assigned_row[minCol[i]] = true;
                pairs.add(new pair(c.getX(cal)[minCol[i]], c.getY(cal)[minCol[i]],c.getZ(cal)[minCol[i]],c.getX2(cal)[i], c.getY2(cal)[i],c.getZ2(cal)[i],  minCol[i],i));
            }

            IJ.showProgress(i,minRow.length-1);

        }


        return pairs;



    }


    private ArrayList<pair> getNNstrickt_twosets(coordinates c, boolean cal){
        if(c instanceof coordinates3D){
            return getNNstrickt_twosets((coordinates3D) c,cal);
        }

        ArrayList<pair> pairs = new ArrayList<>();

        if(c.getX(cal).length < 1 || c.getX2(cal).length < 1){
            pairs.add(new pair(0,0,0,0,-1,-1));
            return pairs;
        }

        boolean[] assigned_row = new boolean[c.getX(cal).length];
        boolean[] assigned_col = new boolean[c.getX2(cal).length];



        boolean test = false;
        int cnt;
        while(!test) {
            cnt = 0;

            int[] minRow = getMinRow(c.getDistMat(cal), assigned_row, assigned_col);
            int[] minCol = getMinCol(c.getDistMat(cal), assigned_row, assigned_col);

            if (c.getX(cal).length <= c.getX2(cal).length) {



                for(int i=0;i<minRow.length;i++){

                    if(minCol[minRow[i]]== i && !assigned_col[minRow[i]] && !assigned_row[i]){
                        assigned_row[i] = true;
                        assigned_col[minRow[i]] = true;
                        pairs.add(new pair(c.getX(cal)[i], c.getY(cal)[i], c.getX2(cal)[minRow[i]], c.getY2(cal)[minRow[i]], i,minRow[i]));

                    }


                }

                for(boolean j:assigned_row){
                    if(j) {
                        cnt++;
                    }
                }

                if(cnt >= assigned_row.length){
                    test = true;
                }


            } else {



                for(int i=0;i<minCol.length;i++){

                    if(minRow[minCol[i]]== i && !assigned_row[minCol[i]] && !assigned_col[i] ){
                        assigned_row[minCol[i]] = true;
                        assigned_col[i] = true;
                        pairs.add(new pair(c.getX(cal)[minCol[i]], c.getY(cal)[minCol[i]], c.getX2(cal)[i], c.getY2(cal)[i], minCol[i], i));

                    }



                }

                for(boolean j:assigned_col){
                    if(j) {
                        cnt++;
                    }
                }

                if(cnt >= assigned_col.length){
                    test = true;
                }
            }
        }

        return(pairs);
    }
    private ArrayList<pair> getNNstrickt_twosets(coordinates3D c, boolean cal){


        ArrayList<pair> pairs = new ArrayList<>();

        if(c.getX(cal).length < 1 || c.getX2(cal).length < 1){
            pairs.add(new pair(0,0,0,0,0,0,-1,-1));
            return pairs;
        }

        boolean[] assigned_row = new boolean[c.getX(cal).length];
        boolean[] assigned_col = new boolean[c.getX2(cal).length];



        boolean test = false;
        int cnt;
        while(!test) {
            cnt = 0;

            int[] minRow = getMinRow(c.getDistMat(cal), assigned_row, assigned_col);
            int[] minCol = getMinCol(c.getDistMat(cal), assigned_row, assigned_col);

            if (c.getX(cal).length <= c.getX2(cal).length) {



                for(int i=0;i<minRow.length;i++){

                    if(minCol[minRow[i]]== i && !assigned_col[minRow[i]] && !assigned_row[i]){
                        assigned_row[i] = true;
                        assigned_col[minRow[i]] = true;
                        pairs.add(new pair(c.getX(cal)[i], c.getY(cal)[i],c.getZ(cal)[i], c.getX2(cal)[minRow[i]], c.getY2(cal)[minRow[i]],c.getZ2(cal)[minRow[i]],i, minRow[i]));

                    }


                }

                for(boolean j:assigned_row){
                    if(j) {
                        cnt++;
                    }
                }

                if(cnt >= assigned_row.length){
                    test = true;
                }


            } else {



                for(int i=0;i<minCol.length;i++){

                    if(minRow[minCol[i]]== i && !assigned_row[minCol[i]] && !assigned_col[i] ){
                        assigned_row[minCol[i]] = true;
                        assigned_col[i] = true;
                        pairs.add(new pair(c.getX(cal)[minCol[i]], c.getY(cal)[minCol[i]],c.getZ(cal)[minCol[i]], c.getX2(cal)[i], c.getY2(cal)[i], c.getZ2(cal)[i], minCol[i], i));

                    }



                }

                for(boolean j:assigned_col){
                    if(j) {
                        cnt++;
                    }
                }

                if(cnt >= assigned_col.length){
                    test = true;
                }
            }
        }

        return(pairs);
    }

    private int[] getMinRow(double[][] distmat, boolean[] assigned_row, boolean[] assigned_col){
        int[] minRow = new int[distmat.length];
        double mindist;

        for(int i=0;i<distmat.length;i++){

            mindist = c.max+1;
            for(int j=0;j<distmat[i].length;j++){

                if(!assigned_row[i] && !assigned_col[j]){
                    if(distmat[i][j]<mindist){
                        mindist = distmat[i][j];
                        minRow[i] = j;

                    }
                }
            }
        }


        return minRow;

    }
    private int[] getMinCol(double[][] distmat,boolean[] assigned_row, boolean[] assigned_col){
        int[] minCol = new int[distmat[0].length];
        double mindist;

        for(int i=0;i<distmat[0].length;i++){
            mindist = c.max+1;
            for(int j=0;j<distmat.length;j++){
                if(!assigned_col[i] && !assigned_row[j]){
                    if(distmat[j][i]<mindist){
                        mindist = distmat[j][i];
                        minCol[i] = j;
                    }
                }
            }
        }

        return minCol;

    }


    private void makeResults(ArrayList<pair> pairset,ArrayList<pair> pairset_cal){
        ResultsTable rt = Analyzer.getResultsTable();

        if (rt == null) {
            rt = new ResultsTable();
        }

        rt.reset();


        for (int i = 0; i < pairset.size(); i++) {

            rt.incrementCounter();
            rt.addValue("x1", pairset.get(i).x1);
            rt.addValue("y1", pairset.get(i).y1);
            if (pairset.get(i).is3D) {
                rt.addValue("z1", pairset.get(i).z1);
            }
            rt.addValue("x2", pairset.get(i).x2);
            rt.addValue("y2", pairset.get(i).y2);
            if (pairset.get(i).is3D) {
                rt.addValue("z2", pairset.get(i).z2);
            }
            rt.addValue("index1", pairset.get(i).i1);
            rt.addValue("index2", pairset.get(i).i2);
            rt.addValue("Distance", pairset.get(i).dist);
            if(pairset_cal!=null) {
                rt.addValue("x1_" + c.unit, pairset_cal.get(i).x1);
                rt.addValue("y1_" + c.unit, pairset_cal.get(i).y1);
                if (pairset.get(i).is3D) {
                    rt.addValue("z1" + c.unit, pairset_cal.get(i).z1);
                }
                rt.addValue("x2_" + c.unit, pairset_cal.get(i).x2);
                rt.addValue("y2_" + c.unit, pairset_cal.get(i).y2);
                if (pairset.get(i).is3D) {
                    rt.addValue("z2" + c.unit, pairset_cal.get(i).z2);
                }
                rt.addValue("index1_" + c.unit, pairset_cal.get(i).i1);
                rt.addValue("index2_" + c.unit, pairset_cal.get(i).i2);
                rt.addValue("Distance_" + c.unit, pairset_cal.get(i).dist);
            }

        }

        Analyzer.setResultsTable(rt);
        rt.updateResults();
        rt.show("Results");


    }


    //Macro Extensions


    public String handleExtension(String s, Object[] objects) {
        Calibration cal = new Calibration();
        ArrayList<pair> pairset=null;
        if(s.equals("getNN2D")) {
            this.c = new coordinates((Object[])objects[0],(Object[])objects[1], cal);
            pairset = getNNloose(c, false);
        }
        if(s.equals("getNN3D")) {
            this.c = new coordinates3D((Object[])objects[0],(Object[])objects[1],(Object[])objects[2],cal);
            pairset = getNNloose(c, false);
        }
        if(s.equals("getNN2D_2set")) {
            this.c = new coordinates((Object[])objects[0], (Object[]) objects[1],(Object[])objects[2], (Object[])objects[3], cal,cal);
            pairset = getNNloose_twosets(c, false);
        }
        if(s.equals("getNN3D_2set")) {
            this.c = new coordinates3D((Object[])objects[0], (Object[])objects[1],(Object[])objects[2],(Object[])objects[3], (Object[])objects[4],(Object[])objects[5], cal,cal);
            pairset = getNNloose_twosets(c, false);
        }
        makeResults(pairset,null);
        return null;
    }

    public ExtensionDescriptor[] getExtensionFunctions() {

        ExtensionDescriptor[] ed = new ExtensionDescriptor[4];
        ed[0] = new ExtensionDescriptor("getNN2D",new int[]{ARG_ARRAY,ARG_ARRAY},this);
        ed[1] = new ExtensionDescriptor("getNN3D",new int[]{ARG_ARRAY,ARG_ARRAY,ARG_ARRAY},this);
        ed[2] = new ExtensionDescriptor("getNN2D_2set",new int[]{ARG_ARRAY,ARG_ARRAY,ARG_ARRAY,ARG_ARRAY},this);
        ed[3] = new ExtensionDescriptor("getNN3D_2set",new int[]{ARG_ARRAY,ARG_ARRAY,ARG_ARRAY,ARG_ARRAY,ARG_ARRAY,ARG_ARRAY},this);

        return ed;
    }
}
