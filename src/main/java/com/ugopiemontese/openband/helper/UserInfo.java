package com.ugopiemontese.openband.helper;

/**
 * Created by UgoRaffaele on 30/01/2015.
 */
public class UserInfo {

    private String btAddress;
    private String alias;
    private int gender;
    private int age;
    private int height;
    private int weight;
    private int type;

    private byte[] data = new byte[20];

    public UserInfo(String address, String alias, int gender, int age, int height, int weight, int type) {

        this.btAddress = address;
        this.alias = alias;
        this.gender = gender;
        this.age = age;
        this.height = height;
        this.weight = weight;
        this.type = type;

        byte[] sequence = new byte[20];

        int uid = Integer.parseInt(alias);

        sequence[0] = (byte) uid;
        sequence[1] = (byte) (uid >>> 8);
        sequence[2] = (byte) (uid >>> 16);
        sequence[3] = (byte) (uid >>> 24);

        sequence[4] = (byte) (gender & 0xff);
        sequence[5] = (byte) (age & 0xff);
        sequence[6] = (byte) (height & 0xff);
        sequence[7] = (byte) (weight & 0xff);
        sequence[8] = (byte) (type & 0xff);

        for (int u = 9; u < 19; u++)
            sequence[u] = alias.getBytes()[u-9];

        byte[] crcSequence = new byte[19];
        for (int u = 0; u < crcSequence.length; u++)
            crcSequence[u] = sequence[u];

        sequence[19] = (byte) ((getCRC8(crcSequence) ^ Integer.parseInt(address.substring(address.length()-2), 16)) & 0xff);

        this.data = sequence;
    }

    public byte[] getData() {
        return this.data;
    }

    protected int getCRC8(byte[] seq) {
        int len = seq.length;
        int i = 0;
        byte crc = 0x00;

        while (len-- > 0) {
            byte extract = seq[i++];
            for (byte tempI = 8; tempI != 0; tempI--) {
                byte sum = (byte) ((crc & 0xff) ^ (extract & 0xff));
                sum = (byte) ((sum & 0xff) & 0x01);
                crc = (byte) ((crc & 0xff) >>> 1);
                if (sum != 0) {
                    crc = (byte)((crc & 0xff) ^ 0x8c);
                }
                extract = (byte) ((extract & 0xff) >>> 1);
            }
        }
        return (crc & 0xff);
    }

}
