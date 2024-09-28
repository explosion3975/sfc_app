package com.example.sfc_front.ui.FDAES;
import android.net.Uri;

import com.example.sfc_front.ui.home.HomeViewModel;
import com.example.sfc_front.ui.library.library;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FDAES {
    //RK is RK0~RK10
    private final int[][] RK = new int[11][16];
    //D_Box is D0_Box~D10_Box
    private final int[][] D_Box = new int[11][256];
    private final int[][] LP;
    private final int[] K_CK = new int[16];
    //input 16~128 byte
    public FDAES(String PW) {
        int[] P = library.StringToInt(PW);

        System.arraycopy(P,0,K_CK,0,16);
        int[] i_Box = {52, 91, 130, 169, 208, 247, 30, 71, 112, 153, 194, 235, 20, 63, 106, 149, 192, 236, 23, 68,
                114, 159, 204, 250, 39, 86, 134, 181, 228, 19, 70, 120, 170, 219, 12, 64, 117, 168, 221, 16, 73,
                126, 180, 234, 34, 90, 146, 203, 4, 61, 123, 183, 242, 45, 105, 167, 230, 37, 100, 164, 229, 40,
                104, 174, 241, 51, 122, 191, 5, 78, 148, 220, 38, 113, 189, 8, 85, 162, 244, 65, 144, 225, 53,
                137, 217, 48, 136, 222, 55, 143, 237, 72, 161, 0, 95, 193, 29, 131, 231, 77, 178, 24, 129, 239,
                88, 198, 49, 163, 18, 138, 254, 111, 233, 98, 216, 92, 213, 89, 215, 97, 232, 110, 253, 135, 21,
                166, 56, 202, 101, 2, 156, 58, 214, 128, 36, 206, 125, 43, 223, 151, 76, 10, 197, 141, 80, 26,
                238, 184, 142, 99, 62, 32, 9, 248, 218, 205, 196, 190, 195, 201, 212, 246, 11, 41, 75, 116, 160,
                224, 31, 102, 175, 14, 107, 200, 66, 177, 59, 187, 93, 3, 165, 96, 42, 252, 207, 176, 171, 173,
                199, 251, 44, 109, 186, 54, 158, 79, 1, 179, 140, 132, 147, 209, 25, 119, 17, 182, 133, 127, 172,
                22, 139, 74, 60, 94, 227, 118, 108, 188, 81, 67, 154, 84, 152, 69, 150, 103, 6, 46, 50, 35, 28,
                47, 145, 57, 157, 226, 7, 240, 87, 83, 82, 13, 210, 121, 15, 33, 27, 245, 211, 124, 115, 155, 185,
                243, 249, 255};
        D_Box[0] = library.GBT1(library.DASM(P, i_Box), i_Box);
        int[] CK_E = library.DASMExpansion(library.DASM(P, i_Box), i_Box);
        int[] KL = library.Middle(CK_E,40,16);
        int[] KR = library.Middle(CK_E,72,16);
        RK[0] =library.XOR(library.BinaryAdd(K_CK,KL),KR);
        D_Box[1] = library.GBT1(RK[0],D_Box[0]);
        for(int i=1;i<10;i++){
            RK[i] = library.DASM(RK[i-1],D_Box[i]);
            D_Box[i+1] = library.GBT2(RK[i],D_Box[i]);
        }
        RK[10] = library.DASM(RK[9],D_Box[10]);
        LP =library.LP(RK);
    }
    public String Encryption(String Plaintext){
        int[] P = library.StringToInt(Plaintext);
        int[] tmp = library.XOR(P,this.RK[0]);
        int[] C;
        for(int i=1;i<10;i++){
            library.SubBytes(tmp, D_Box[i]);
            tmp = library.DBR(tmp,this.LP[i-1][0]);
            tmp = library.MixColumn(tmp,this.LP[i-1][1]);
            tmp = library.XOR(tmp,this.RK[i]);
        }
        library.SubBytes(tmp, D_Box[10]);
        tmp = library.DBR(tmp,this.LP[9][0]);
        C = library.XOR(tmp,this.RK[10]);

        return library.IntToString(C);
    }
    public String Decryption(String Ciphertext){
        int[] C = library.StringToInt(Ciphertext);
        int[] tmp = library.XOR(C,this.RK[10]);
        tmp = library.InverseDBR(tmp,this.LP[9][0]);
        library.SubBytes(tmp, library.InverseBox(D_Box[10]));
        for(int i=9;i>0;i--){
            tmp = library.XOR(tmp,this.RK[i]);
            tmp = library.InverseMixColumn(tmp,this.LP[i-1][1]);
            tmp = library.InverseDBR(tmp,this.LP[i-1][0]);
            library.SubBytes(tmp,library.InverseBox(D_Box[i]));
        }
        int[] P=library.XOR(tmp,this.RK[0]);
        return library.IntToString(P);
    }
    //FileEncryption(input file,output file)
    public void FileEncryption_CBC(File filePath,File EncFile){
        try (InputStream inputStream = new BufferedInputStream(Files.newInputStream(filePath.toPath()))) {
            try (OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(EncFile.toPath()))) {
                int[] IV = this.K_CK.clone();
                byte[] buffer = new byte[524288];
                int[] enc = IV;
                int bytesRead;
                long fileSize = filePath.length();
                long ii = 0;
                HomeViewModel.currentProgress =0;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    ii+=bytesRead;
                    HomeViewModel.currentProgress = (int) (ii*100/fileSize);
                    int[] FileBlock = library.ByteTOInt(buffer);
                    int group = bytesRead / 16;
                    if (bytesRead % 16 != 0) {
                        group += 1;
                    }
                    int[] output = new int[group*16];
                    for (int i = 0; i < group; i++) {
                        int[] tmp = new int[16];
                        if (i != group - 1) {
                            System.arraycopy(FileBlock, i * 16, tmp, 0, 16);
                        } else {
                            int lack = bytesRead % 16;
                            if (lack != 0) {
                                System.arraycopy(FileBlock, i * 16, tmp, 0, lack);
                                int paddingLength = 16 - lack;
                                for (int j = 0; j < paddingLength; j++) {
                                    tmp[lack + j] = paddingLength;
                                }
                            } else {
                                System.arraycopy(FileBlock, i * 16, tmp, 0, 16);
                            }
                        }
                        tmp = library.XOR(enc,tmp);
                        enc = library.StringToInt(this.Encryption(library.IntToString(tmp)));
                        System.arraycopy(enc, 0, output, i * 16, 16);
                    }
                    outputStream.write(library.IntTOByte(output));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void FileDecryption_CBC(File filePath,File DecFile)  {
        try (InputStream inputStream = new BufferedInputStream(Files.newInputStream(filePath.toPath()))) {
            try (OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(DecFile.toPath()))) {
                long totalSize =  filePath.length();
                long curSize = 0;
                int[] IV = this.K_CK.clone();
                byte[] buffer = new byte[524288];
                int[] dec;
                int bytesRead;
                int[] tmp = new int[16];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    curSize+=bytesRead;
                    int[] FileBlock = library.ByteTOInt(buffer);
                    if (bytesRead > 16) {
                        int[] output = new int[bytesRead - 16];
                        int group = bytesRead / 16;
                        for (int i = 0; i < group; i++) {
                            System.arraycopy(FileBlock, i * 16, tmp, 0, 16);
                            dec = library.StringToInt(this.Decryption(library.IntToString(tmp)));
                            dec = library.XOR(dec,IV);
                            IV = tmp.clone();
                            if (i == group - 1) {
                                int end = 15;
                                //有問題換成bytesRead!=524288
                                if(curSize==totalSize) {
                                    int last = dec[15];
                                    int num = 1;
                                    for (int j = 14; j >= 0; j--) {
                                        if (dec[j] != last) {
                                            end = j;
                                            break;
                                        }
                                        num++;
                                    }
                                    if (num < last) {
                                        end = 15;
                                    } else if (num > last) {
                                        end = 15 - last;
                                    }
                                }
                                int[] combined = new int[end + bytesRead - 15];
                                System.arraycopy(output, 0, combined, 0, bytesRead - 16);
                                System.arraycopy(dec, 0, combined, bytesRead - 16, end + 1);
                                outputStream.write(library.IntTOByte(combined));
                                break;
                            }
                            System.arraycopy(dec, 0, output, i * 16, 16);
                        }
                    } else {
                        System.arraycopy(FileBlock, 0, tmp, 0, 16);
                        dec = library.StringToInt(this.Decryption(library.IntToString(tmp)));
                        dec = library.XOR(dec,IV);
                        IV = tmp.clone();
                        int[] ori;
                        int end = 15;
                        int last = dec[15];
                        int num = 1;
                        for (int j = 14; j >= 0; j--) {
                            if (dec[j] != last) {
                                end = j;
                                break;
                            }
                            num++;
                        }
                        if(num<last){
                            end=15;
                        }else if(num>last){
                            end=15-last;
                        }
                        ori = new int[end + 1];
                        System.arraycopy(dec, 0, ori, 0, end + 1);
                        outputStream.write(library.IntTOByte(ori));
                    }
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
