

/**
 * Created by stanislavtyrsa on 21.11.16.
 */
class Looper {
    Closure code
    static Looper loop(Closure code){
        new Looper(code:code)
    }
    void until(Closure test){
        code()
        while (!test()){
            code()
        }
    }
}
