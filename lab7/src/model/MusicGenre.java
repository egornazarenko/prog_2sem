package model;

import java.io.Serializable;

public enum MusicGenre implements Serializable {
    RAP,
    SOUL,
    POP;

    public static MusicGenre fromNum(int number) {
        switch (number) {
            case 1: return RAP;
            case 2: return SOUL;
            case 3: return POP;
            default: return null;
        }
    }
}
