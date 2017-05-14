
package ant;

import java.util.Scanner;
import java.math.*;
public class SACO{
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
    //
    //init variables
    //
        int nt = 10000;//max number of iterations
        int t0 = 1;//initial pheromone
        double all = 0,distance = 0;
        int count=0;
        double stop_cond = 0; 
        System.out.print("input \"the number of nodes\" : ");
        int nodenum = input.nextInt();
        System.out.println("input \"edges\" : ");
        Map map = new Map(nodenum);
        for(int i = 0;i<map.size;i++){
            for(int j = 0;j<map.size;j++){
                int tmp = input.nextInt();
                map.setMap(i, j, tmp);
            }
        }
        map.initTau(t0);                        //初始費洛蒙
        System.out.println("\ninit pheromone: ");
        map.printTau();                         //print初始的費洛蒙
        System.out.print("\ninput \"the start node\" : ");
        int start = input.nextInt();
        System.out.print("input \"the end node \" : ");
        int end = input.nextInt();
        System.out.print("input \"the number of ants\" : ");
        int nk = input.nextInt();
        Ant[] ant = new Ant[nk];
        for(int i = 0;i<nk;i++){
            ant[i] = new Ant(start,end, nodenum);
        }
        double[] tmpP = new double[map.size];
        int t = 0;//iterations count 
        while(t <= nt ){
            //
            //----------ant search road----------------
            //
            for(int i = 0;i < nk;i++){    
                while(ant[i].now != ant[i].end){
                    if(!hasNext(map,ant[i]) ){
                        ant[i].goback();
                    }
                    else{
                        for(int j = 0;j<tmpP.length;j++){
                            tmpP[j]=0;
                        }
                        all = 0;
                        for(int j = 0;j<map.size;j++){
                            if(map.isLink(ant[i].now, j) && !ant[i].isInPathstack(j)){
                                tmpP[j] = map.calcuTijt(ant[i].now, j);     //算出機率
                                tmpP[j] = tmpP[j] * Math.random()*5;    //隨機加成
                                all+=tmpP[j];
                            }
                        }
                        double max = tmpP[0];           
                        int next = 0;
                        for(int j = 0;j<map.size;j++){
                            if(tmpP[j]>max) {       //選機率加乘後的最高機率
                                max = tmpP[j];
                                next = j;
                            }
                        }
                        ant[i].move(next);
                    }
                }
            }
            //
            //---------update tau--------------------
            //
            map.reduceTau();    //費洛蒙消散
            all = 0;
            for(int i = 0;i<nk;i++){
               distance = 2.0/Math.pow(ant[i].ps_now,2);    //路徑長倒數*2
               for(int j = 0;j<ant[i].ps_now-1;j++){
                   map.updateTau(ant[i].pathstack[j], ant[i].pathstack[j+1], distance);
               }
           }
            //
            //--------is end?----------------------
            //
            count = 0;
            for(int i = 0;i<nk;i++){
                if(isSamePath(ant[0],ant[i])){
                    count++;
                }
            }
            stop_cond = (double)count/nk*100;
            if(stop_cond>=90)   break;
            //
            //---------start to end or end to start
            //
            for(int i = 0;i<nk;i++){
                if(ant[i].start == start)
                    ant[i].reset(end,start,map.size);
                else 
                    ant[i].reset(start,end,map.size);
            }
            t++;
        }
        System.out.println("\n\n------------output-------------");
        System.out.println("\nα: "+map.alpha+"\nρ: "+map.rho);
        System.out.println("\nnumber of iterations: "+t+"\n");   
        System.out.println("\nfinal Tau: ");
        map.printTau();
        System.out.println("\nthe Best Path : ");
        ant[0].printPath(start);
        System.out.println("\nthe length of Path :"+ant[0].ps_now);
    }
    //有沒有下一個沒走過的點
    public static boolean hasNext(Map map,Ant ant){
        for(int i = 0;i<map.size;i++){
            if(map.isLink(ant.now, i) && !ant.isInPathstack(i)){
                return true;
            }
        }
        return false;
    }
    //ant a 和 b 的路徑是否相同
    public static boolean isSamePath(Ant a,Ant b){
        for(int i = 0;i<b.ps_now;i++){
            if(a.pathstack[i] != b.pathstack[i]) return false;
        }
        return true;
    }
}
//----------------------地圖class--------------------
class Map{
    public int size;
    public int[][] link ;
    public double[][] tau;
    public int alpha = 2;       //α:pheromone intensification
    public double rho = 0.5;    //ρ: pheromone evaporation
    public Map(int nodes){
        this.size = nodes;
        this.link = new int [size][size];
        this.tau = new double [size][size];
    }
    public void setMap(int i,int j){
        this.link[i][j] = 1;
    }
    public void setMap(int i, int j,int s){
        this.link[i][j] = s;
    } 
    public boolean isLink(int i,int j){
        return this.link[i][j] == 1;
    }
    public void initTau(double t0){
        for(int i = 0;i < size; i++){
            for(int j = 0; j<size;j++){
                if(this.isLink(i, j)){
                    tau[i][j] = Math.random()*t0;
                    if(this.isLink(j, i))
                        tau[j][i] = tau[i][j];
                }
                else 
                    tau[i][j] = 0;
            }
        }
    }
    public void updateTau(int i, int j, double t){
        tau[i][j] += t;
        if(this.isLink(j,i))    tau[j][i] += t;
    }
    public void reduceTau(){
        for(int i = 0;i<size;i++){
            for(int j = 0;j<size;j++){
                tau[i][j] = (1-rho)*tau[i][j];
            }
        }
    }
    public double calcuTijt(int i,int j){
        return Math.pow(this.tau[i][j],this.alpha);
    }
    public void printMap(){
        for(int i = 0;i<size;i++){
            for(int j = 0;j<size;j++){
                System.out.print(link[i][j]+" ");
            }
            System.out.println();
        }
    }
    public void printTau(){
        for(int i = 0;i<size;i++){
            for(int j = 0;j<size;j++){
                System.out.printf("%5.3f  ",tau[i][j]);
            }
            System.out.println();
        }
    }
}
//--------------------Ant class----------------------------
class Ant
{
    public int now;         //螞蟻現在位置
    public int[] pathstack; //螞蟻走過的路
    public int ps_now = 0;  //pathstack長度
    public int start,end;   //起點/終點

    public Ant(int start, int end,int nodenum){
        this.start = start;
       now = this.start;
       pathstack = new int[nodenum];
       for(int i = 0;i<nodenum;i++){
           pathstack[i] = nodenum;
       }
       pathstack[0]=start;
       this.end = end;
       ps_now = 1;
    }
    public void reset(int start,int end,int nodenum){
        for(int i =0;i<pathstack.length;i++){
            pathstack[i] = nodenum;
        }
        pathstack[0] = start;
        ps_now = 1;
        now = start;
        this.start = start;
        this.end = end;
    }
    public void move(int j){
        now = j;
        pathstack[ps_now] = now;
        ps_now++;
    }
    public boolean isInPathstack(int j){
        for(int i = 0;i<pathstack.length;i++){
            if(pathstack[i] == j)   return true;
        }
        return false;
    }
    public void goback(){
        //pathstack[ps_now] = pathstack[ps_now-1];
        now = pathstack[ps_now-1];
        ps_now--;
    }
    public void printPath(int s){
        if(pathstack[0] != s){
            for(int i = ps_now-1;i>=0;i--){
                System.out.print(pathstack[i]+" ");
            }
        }
        else{
            for(int i = 0;i<ps_now;i++){
                System.out.print(pathstack[i]+" ");
            }
        }
        System.out.println();
    }
}

