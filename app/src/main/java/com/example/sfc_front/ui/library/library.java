package com.example.sfc_front.ui.library;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


public class library {
    //直接修改P就不會再需要一個記憶體
    public static int[] SubBytes(int[] P, int[] D_Box) {
//        int[] outcome = new int[P.length];
        for (int i = 0; i < P.length; i++) {
            P[i] = D_Box[P[i]];
        }
        return P;
    }
    public static int[] InverseBox(int[] Box) {
        int length = Box.length;
        int[] inv_box = new int[length];
        for (int i = 0; i < length; i++) {
            inv_box[Box[i]] = i;
        }
        return inv_box;
    }
    public static int[] XOR(int[] n1, int[] n2) {
        int length = n1.length;
        int[] outcome = new int[length];
        if (length != n2.length) {
            throw new IllegalArgumentException("The two have different lengths.");
        }
        for (int i = 0; i < length; i++) {
            outcome[i] =  (n1[i] ^ n2[i]);
        }
        return outcome;
    }
    public static int[] BinaryAdd(int[] n1, int[] n2) {
        int length = n1.length;
        int[] n3 = new int[length];
        int sum, index, carry = 0;
        if (length != n2.length) {
            throw new IllegalArgumentException("The two have different lengths.");
        }
        for (int i = length - 1; i >= 0; i--) {
            sum = n1[i] + n2[i] + carry;
            carry = sum >> 8;
            index = sum % 256;
            n3[i] = table.S_Box[index];
        }
        return n3;
    }
    public static int[] BinarySub(int[] n3, int[] n2) {
        int length = n3.length;
        int[] n1 = new int[length];
        int borrow = 0, sub;
        if (length != n2.length) {
            throw new IllegalArgumentException("The two have different lengths.");
        }
        for (int i = length - 1; i >= 0; i--) {
            sub = table.Inverse_S_Box[n3[i]] - n2[i] - borrow;
            borrow = sub < 0 ? 1 : 0;
            n1[i] =  (sub + (borrow << 8));
        }
        return n1;
    }
    public static int[] Rotate(int[] n1, int[] n2) {
        int length = n1.length, block_length = length / 4;
        int[] tmp = new int[block_length];
        System.arraycopy(n1, length - block_length, tmp, 0, block_length);
        for (int i = length - 1; i >= block_length; i--) {
            n1[i] = n1[i - block_length];
        }
        System.arraycopy(tmp, 0, n1, 0, block_length);
        return XOR(n1, n2);
    }
    public static int[] InverseRotate(int[] n3, int[] n2) {
        int length = n3.length, block_length = length / 4;
        int[] tmp = new int[block_length];
        int[] n1 = XOR(n3, n2);
        int[] outcome = new int[length];
        System.arraycopy(n1, 0, tmp, 0, block_length);
        System.arraycopy(n1, block_length, outcome, 0, length - block_length);
        System.arraycopy(tmp, 0, outcome, length - block_length, block_length);
        return outcome;
    }
    public static int[] DASM(int[] P, int[] D_Box) {
        int length = P.length;
        long[] vp = new long[length + 2];
        long dsc = 256;
        int[] C = new int[length];
        for (int i = 1; i <= length; i++) {
            vp[i] = P[i - 1];
            dsc = dsc + (vp[i] + 1) * i;
        }
        vp[0] = vp[1] + vp[length] + dsc;
        vp[length + 1] = vp[length] + vp[length - 1];
        for (int i = 1; i <= length; i++) {
            dsc = (dsc + vp[i - 1] * vp[i] + vp[i + 1] + i) % 1048576;
            int vch = (int) ((vp[i] + dsc) % 256);
            C[i - 1] = D_Box[vch];
        }
        return C;
    }

    public static int[] StringToInt(String str) {
        int length = str.length();
        int[] outcome = new int[length];
        for(int i=0;i<length;i++){
            char c = str.charAt(i);
            outcome[i] = c;
        }
        return outcome;
    }
    public static String IntToString(int[] c) {
        StringBuilder str = new StringBuilder();
        for (int j : c) {
            str.append((char) j);
        }
        return str.toString();
    }
    public static int[] Middle(int[] CK_E, int Start, int length) {
        int[] K = new int[length];
        System.arraycopy(CK_E, Start, K, 0, length);
        return K;
    }
    public static int[] DASMExpansion(int[] PW, int[] D_box) {
        while (PW.length < 1024) {
            int[] P = DASM(PW, D_box);
            int length = P.length;
            int[] PWCopy = PW.clone();
            PW = new int[length * 3];
            System.arraycopy(P, 0, PW, 0, length);
            System.arraycopy(PWCopy, 0, PW, length, length);
            System.arraycopy(P, 0, PW, length + length, length);
        }
        PW = DASM(PW, D_box);
        int[] outcome = new int[128];
        System.arraycopy(PW, PW.length - 128, outcome, 0, 128);
        return outcome;
    }
    private static int[][] split128(int[] PW) {
        int row_size = PW.length % 128 == 0 ? PW.length : PW.length + 1;
        int[][] outcome = new int[row_size][128];
        for (int i = 0; i < row_size; i++) {
            System.arraycopy(PW, 128 * i, outcome[i], 0, 128);
        }
        return outcome;
    }
    public static int[][] GISK(int[] PW, int[] D_box) {
        int[] ISK1;
        int[][] outcome = new int[2][128];
        int PWLength = PW.length;
        if (PWLength < 128) {
            ISK1 = DASMExpansion(PW, D_box);
        } else if (PWLength == 128) {
            ISK1 = DASM(PW, D_box);
        } else {
            int[][] splitPW = split128(PW);
            int m = splitPW.length - 1;
            if (PWLength % 128 != 0) {
                splitPW[m] = DASMExpansion(splitPW[m], D_box);
            }
            ISK1 = splitPW[0];
            for (int i = 1; i < m + 1; i++) {
                ISK1 = XOR(ISK1, splitPW[i]);
            }
        }
        outcome[0] = ISK1;
        outcome[1] = DASM(ISK1, D_box);
        return outcome;
    }
    public static int[][] GDK(int[] ISK1, int[] ISK2, int[] D_box) {
        int[][] outcome = new int[3][128];
        outcome[0] = DASM(DASM(XOR(ISK1, ISK2), D_box), D_box);
        outcome[1] = DASM(DASM(BinaryAdd(ISK1, ISK2), D_box), D_box);
        outcome[2] = DASM(DASM(Rotate(ISK1, ISK2), D_box), D_box);
        return outcome;
    }
    public static int[] DBG(int[] KA, int[] KB, int[] KC) {
        int[] SIA1, SIA2, SIA3, RIA, D_box;
        SIA1 = new int[128];
        SIA2 = new int[256];
        SIA3 = new int[256];
        RIA = new int[256];
        D_box = new int[256];
        boolean[] FA = new boolean[256];
        int AL1 = 0, AL2 = 0, ALR = 0;
        for (int i = 0; i < 128; i++) {
            if (!FA[KA[i]]) {
                SIA1[AL1] = KA[i];
                FA[KA[i]] = true;
                AL1++;
            }
        }
        for (int i = 0; i < 128; i++) {
            if (!FA[KB[i]]) {
                SIA2[AL2] = KB[i];
                FA[KB[i]] = true;
                AL2++;
            }
        }
        for (int i = 0; i < 128; i++) {
            if (!FA[KC[i]]) {
                SIA2[AL2] = KC[i];
                FA[KC[i]] = true;
                AL2++;
            }
        }
        for (int i = 0; i < 256; i++) {
            if (!FA[i]) {
                RIA[ALR] = i;
                ALR++;
            }
        }
        System.arraycopy(RIA, 0, SIA2, AL2, ALR);
        System.arraycopy(SIA1, 0, SIA2, AL2 + ALR, AL1);
        int h1 = 0, h2 = 0, h3 = 0, jup1 = 0, jup2 = 0, jup3 = 0;
        for (int i = 0; i < 64; i++) {
            h1 += KB[i];
            jup1 += KB[64 + i];
        }
        for (int i = 0; i < 32; i++) {
            h2 += KC[i];
            h3 += KC[32 + i];
            jup2 += KC[64 + i];
            jup3 += KC[96 + i];
        }
        h1 = h1 % 256;
        h2 = h2 % 256;
        h3 = h3 % 256;
        jup1 = jup1 % 128;
        if (jup1 % 2 == 0) {
            jup1 += 129;
        }
        jup2 = jup2 % 128;
        if (jup2 % 2 == 0) {
            jup2 += 129;
        }
        jup3 = jup3 % 128;
        if (jup3 % 2 == 0) {
            jup3 += 129;
        }
        for (int i = 0; i < 256; i++) {
            int tmp = (h1 + i * jup1) % 256;
            SIA3[i] = SIA2[tmp];
        }
        for (int i = 0; i < 256; i++) {
            int tmp1 = (h3 + i * jup3) % 256;
            int tmp2 = (h2 + i * jup2) % 256;
            D_box[tmp1] = SIA3[tmp2];
        }
        return D_box;
    }
    public static int[] GBT1(int[] PW, int[] I_box) {
        int[][] ISK = GISK(PW, I_box);
        int[][] DK = GDK(ISK[0], ISK[1], I_box);
        //return D_box;
        return DBG(DK[0], DK[1], DK[2]);
    }
    public static int[] GBT2(int[] K, int[] I_box) {
        int h1 = 0, h2 = 0, jump1 = 0, jump2 = 0;
        int[] D_box = new int[256];
        for (int i = 0; i < 4; i++) {
            h1 += K[i];
            h2 += K[i + 4];
            jump1 += K[i + 8];
            jump2 += K[i + 12];
        }
        h1 = h1 % 256;
        h2 = h2 % 256;
        jump1 = jump1 % 128;
        jump2 = jump2 % 128;
        if (jump1 % 2 == 0) {
            jump1 += 129;
        }
        if (jump2 % 2 == 0) {
            jump2 += 129;
        }
        for (int i = 0; i < 256; i++) {
            int tmp = (h2 + jump2 * i) % 256;
            int tmp1 = (h1 + jump1 * i) % 256;
            D_box[tmp] = I_box[tmp1];
        }
        return D_box;
    }
    public static int[] DBR(int[] P, int n) {
        int[] C = new int[16], nth = new int[16];
        int index;
        if (n < 16) {
            for (int i = 0; i < 16; i++) {
                index = (i + n) % 16;
                C[i] = P[index];
            }
            return C;
        } else if (n < 40) {
            nth = table.DBRArrayType2[n - 16];
        } else if (n < 64) {
            nth = table.DBRArrayType3[n - 40];
        }
        for (int i = 0; i < 16; i++) {

            index = nth[i];
            C[i] = P[index];
        }
        return C;
    }
    public static int[] InverseDBR(int[] C, int n) {
        int[] P = new int[16];
        int[] nth = new int[16];
        int index;
        if (n < 16) {
            for (int i = 0; i < 16; i++) {
                index = (i - n + 16) % 16;
                P[i] = C[index];
            }
            return P;
        } else if (n < 40) {
            nth = table.InverseDBRArrayType2[n - 16];
        } else if (n < 64) {
            nth = table.InverseDBRArrayType3[n - 40];
        }
        for (int i = 0; i < 16; i++) {
            index = nth[i];
            P[i] = C[index];
        }
        return P;
    }
    public static int[] MixColumn(int[] P, int n) {
        int[] outcome = new int[16];
        int[] matrix = table.MixColumn[n];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                int tmp = 0;
                for (int k = 0; k < 4; k++) {
                    int add = -1;
                    int C = matrix[j + 4 * k] ;
                    if(C==0){
                        add=0;
                    } else if (C==1) {
                        add = P[4 * i + k];
                    } else {
                        C-=2;
                        add = MixColumnTable.MulTable[C % 20][P[4 * i + k]];
                    }
                    tmp = tmp^add;
                }
                outcome[i * 4 + j] = tmp;
            }
        }
        return outcome;
    }
    public static int[] InverseMixColumn(int[] C, int n) {
        int[] outcome = new int[16];
        int[] matrix = table.InverseMixColumn[n];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                int tmp = 0;
                for (int k = 0; k < 4; k++) {
                    int add = -1;
                    int P = matrix[j + 4 * k] ;
                    if(P==0){
                        add=0;
                    } else if (P==1) {
                        add = C[4 * i + k];
                    } else {
                        P-=2;
                        add = MixColumnTable.MulTable[P % 20][C[4 * i + k]];
                    }
                    tmp = tmp^add;
                }
                outcome[4 * i + j] =  tmp;
            }
        }
        return outcome;
    }
    public static int[][] LP(int[][] RK) {
        int[][] LP = new int[10][2];
        for (int i = 0; i < 10; i++) {
            int[] tmp = RK[i];
            int tmpLP0 = 0, tmpLP1 = 0;
            for (int j = 0; j < 8; j++) {
                tmpLP0 += tmp[2 * j];
                tmpLP1 += tmp[2 * j + 1];
            }
            tmpLP0 = tmpLP0 % 64;
            tmpLP1 = tmpLP1 % 96;
            LP[i][0] =  tmpLP0;
            LP[i][1] =  tmpLP1;
        }
        return LP;
    }
    public static String FileNameSplit(String filePath) {
        return filePath.substring(filePath.lastIndexOf('\\'));
    }

    public static byte[] IntTOByte(int[] outcome) {
        int length = outcome.length;
        byte[] buffer = new byte[length];
        for (int i = 0; i < length; i++) {
            buffer[i] = (byte) outcome[i];
        }
        return buffer;
    }

    public static int[] ByteTOInt(byte[] buffer) {
        int length = buffer.length;
        int[] outcome = new int[length];
        for (int i = 0; i < length; i++) {
            outcome[i] = Byte.toUnsignedInt(buffer[i]);
        }
        return outcome;
    }
    public static int getIncorrectPasswordAttempts(){
        try {
            String Filename = ".\\src\\main\\java\\com\\example\\sfc_front\\ui\\library\\data.json";
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(new File(Filename));

            return rootNode.get("IncorrectPasswordAttempts").asInt();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }
    public static void writeData(String key,String value){
        try {
            String Filename = ".\\src\\main\\java\\com\\example\\sfc_front\\ui\\library\\data.json";
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(new File(Filename));

            // 修改JSON对象的属性
            ((ObjectNode) rootNode).put(key, value);

            // 将更新后的JSON写回文件
            objectMapper.writeValue(new File(Filename), rootNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
