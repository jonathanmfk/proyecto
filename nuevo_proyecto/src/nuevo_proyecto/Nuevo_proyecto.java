package nuevo_proyecto;

import java.io.*;
import java.io.FileReader;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

public class Nuevo_proyecto {

    /**
     * @param args the command line arguments
     */
    
    
    public static void main(String[] args) {
        int neutra=0,positiva=0,negativa=0;
        int incorrectos=0, correctos=0;
        String nulos="\n";
        try {
            for (int kkk = 1; kkk <= 60; kkk++) {
                double[] pol = new double[2];
                BufferedReader bf = new BufferedReader(new FileReader("C:\\TreeTagger\\prueba_oraciones\\1_("+kkk+").txt-tg.txt"));
                String tmp = "";
                String bfRead;
                String[] aux1;
                String[] tag;
                String[] lemma;
                String aux2 = "";
                String aux3 = "";
                String aux4 = "";
                while ((bfRead = bf.readLine()) != null) {
                    tmp = tmp + bfRead + "\t";
                }
                aux1 = tmp.split("\\t");
                for (int i = 0; i < aux1.length; i++) {
                    if (i % 3 == 1) {
                        aux2 = aux2 + aux1[i] + " ";
                        aux3 = aux3 + aux1[i + 1] + " ";
                        aux4 = aux4 + aux1[i - 1] + " ";
                    }
                }
                tag = aux2.split(" ");
                lemma = aux3.split(" ");
                System.out.print(kkk+") "+aux4+" Lemas:"+lemma.length);
                if (verificarVerbo(tag, 0, 0)) {
                    System.out.print(" sintaxis: Es correcto ");correctos++;
                    pol = Tf_Idf(lemma);
                } else {
                    System.out.print(" sintaxis: incorrecto ");incorrectos++;
                    pol[0]=2;
                }
                if (pol[0] >=0 && pol[0] < 0.024  || pol[0] > -0.024 && pol[0] <=0) {
                    System.out.print(" Polaridad: Neutra ");neutra++;
                    
                }else if (pol[0] <= -0.025 && pol[0] <0.00) {
                    System.out.print(" Polaridad: Negativa ");negativa++;
                }else if (pol[0] >= 0.025 && pol[0]>0.00 && pol[0]<=1) {
                    System.out.print(" Polaridad: Positiva");positiva++;
                }else{
                    nulos=nulos+""+kkk+")"+aux4+"\n";
                }
                System.out.print(" Numero: " + pol[0]);                
                System.out.println("\n");
            }
            System.out.println("neutros: "+neutra+" Positivos: "+positiva+" negativos: "+negativa);
            System.out.println("sintaxis incorrecta "+incorrectos+" correctos: "+correctos);
            System.out.println("nulos: "+nulos);
        } catch (Exception e) {
            System.err.println("Error no se encontró el archivo: " + e);
        }
    }

    public static boolean verificarVerbo(String[] tag, int c, int i) {
        if (i > tag.length - 1) {
            return true;
        }
        if (i == 0) {
            if (validar0(tag)) {
                return true;
            }
        }
        if (c == 2) {
            return false;
        }
        if (tag[i].equals("VLadj") || tag[i].equals("VLfin") || tag[i].equals("VLger") || tag[i].equals("VLinf")) {
            c = c + 1;
        }
//            System.out.println(c+" "+i );
        return verificarVerbo(tag, c, i++);
    }
    // Metodo para validar (verbo-sustantivo) en una palabra
    public static boolean validar0(String[] tag) {
        if (tag[0].equals("VLadj") || tag[0].equals("VLfin") || tag[0].equals("VLinf")) {
            return false;
        }
        if (tag[0].equals("VLger")) {
            switch (tag[1]) {
                case "VLfin":
                    if (!tag[2].equals("ART") && !tag[3].equals("NC")) {
                        return false;
                    }
                    break;
                case "VHfin":
                    if (!tag[2].equals("VLadj") && !tag[3].equals("NC")) {
                        return false;
                    }
                    break;
                case "VSfin":
                    if (!tag[2].equals("ADV") && !tag[3].equals("ADJ")) {
//                        System.out.println("entra el return");
                        return false;
                    }
                    break;
            }
        }
        return true;
    }

    public static double[] Tf_Idf(String[] lemma) {
        Map<String, Integer> map = new TreeMap<String, Integer>();
        for (int i = 0; i < lemma.length; i++) {
            String string = lemma[i];
            map.put(string, (map.get(string) == null ? 1 : (map.get(string) + 1)));
        }
        Object[] a = map.keySet().toArray();
        String[] palabra = new String[a.length];
//        System.out.println("palabras repedidas: " + map+ " "+a.length);
        double[] pol = new double[a.length];
        int contadorPol = 0;
        for (int i = 0; i < a.length; i++) {
            palabra[i] = (String) a[i];
            pol[i] = polaridad(palabra[i]);
            if (pol[i] != 0.0) {//solo cuenta cuando se halla una polaridad
                contadorPol++;
            }
//            System.out.println("polaridad oficial "+pol[i]);
        }

        //Tf = Frecuencia de Término: la cantidad de veces que aparece una palabra en el documento siendo estudiado
        int t = lemma.length;
        double[] tf = new double[t];
        double[] idf = new double[t];
        float[] tf_idf = new float[t];
        int con = 0;
        double aux;
        for (Map.Entry m : map.entrySet()) {
            aux = Integer.parseInt(m.getValue().toString());
            tf[con] = aux / t;
            idf[con] = Math.log10(t / aux);
            tf_idf[con] = (float) (tf[con] * idf[con]);
//            System.out.println(palabra[con]+" tf: "+tf[con] + " idf " + idf[con]+ " aux: "+aux+ " tamaño: "+t);
//            System.out.println("--" + tf_idf[con]);
            con++;
        }
        double[] retorno = asignacionPolaridad(pol, tf_idf, contadorPol);
        return retorno;
//        System.out.println("yy: "+yy);//más en positivos (no)   
//        System.out.println("zz: "+zz);//neutros en postivos(si) 
    }

    public static double[] asignacionPolaridad(double[] pol, float [] lvl, int contadorPol) {
        double[] retorno = new double[2];
//        System.out.println("lvl.length " + lvl.length+ " polaridad.legth "+pol.length);
        if (contadorPol != 0) { //signica que se encontraron por lo menos una polaridad
            for (int i = 0; i < pol.length; i++) {
                if (pol[i] != 0.0) {
//                    System.out.println(pol[i] +" lvl "+ lvl[i]);
                    pol[i] = pol[i] * lvl[i];
//                    System.out.println("eeeeeeeeeeeee"+pol[i]);
                }
            }
            double oficial = 0;
            for (int i = 0; i < pol.length; i++) {
                if (pol[i] != 0.0) {
                    oficial = oficial + pol[i];
                    if(oficial==0){
                        System.out.println("entra");
                        int j=pol.length-1;
                        while (true) {                            
                            if(pol[j]!=0){
                                retorno[0] = retorno[1]= pol[j];
                                return retorno;
                            }
                            j--;  
                        }
                    }
                }
            }
            retorno[0] = oficial; // lvl.length;//division por palabras repetidas
            retorno[1] = oficial; // pol.length;//division por total de palabras
        }
        if (contadorPol == 0) {//Si es 0 singnifica que no se hallaron palabras polarizadas
//            System.out.println("no se hallaron palabras polarizadas");
            return pol;
        }
        return retorno;
    }

    public static double polaridad(String palabra) {
        final String XML = "C:\\Users\\pc\\Desktop\\senticon.es.xml"; //Ruta del Senticon.es.xml
        SAXBuilder builder = new SAXBuilder();
        File xmlFile = new File(XML);
        try {
            Document document = (Document) builder.build(xmlFile);
            Element rootNode = document.getRootElement();
            List list = rootNode.getChildren("layer");
            for (int i = 0; i < list.size(); i++) {
                Element layer = (Element) list.get(i);
                List posi_nega = layer.getChildren();
                for (int j = 0; j < posi_nega.size(); j++) {
                    Element estado_p_n = (Element) posi_nega.get(j);
                    List lemmas = estado_p_n.getChildren();
                    for (int k = 0; k < lemmas.size(); k++) {
                        Element lemma = (Element) lemmas.get(k);
                        String a = lemma.getTextTrim();
//                        a = quitaDiacriticos(a);
                        palabra = palabra.replaceAll(" ", "");
                        if (a.equals(palabra)) {
                            double pol = lemma.getAttribute("pol").getDoubleValue();
//                            System.out.println(palabra + "  polaridad de:  " + pol);
                            return pol;
                        }
                    }
                }
            }
        } catch (IOException io) {
            System.out.println(io.getMessage());
        } catch (JDOMException jdomex) {
            System.out.println(jdomex.getMessage());
        }
        return 0;
    }
}
