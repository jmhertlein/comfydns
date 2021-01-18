package cafe.josh.comfydns;

public class HiResMSTimer {
    private boolean startSet, endSet;
    private long start, end;

    public HiResMSTimer() {
        start = -1;
        end = -1;
        startSet = false;
        endSet = false;
    }

    public void start() {
        if(startSet) {
            throw new IllegalStateException("You already called start()");
        }
        this.start = System.nanoTime();
        this.startSet = true;
    }

    public void end() {
        if(endSet) {
            throw new IllegalStateException("You already called end()");
        }
        this.end = System.nanoTime();
        this.endSet = true;
    }

    public double duration() {
        if(!startSet) {
            throw new IllegalStateException("You forgot to call start()");
        }

        if(!endSet) {
            throw new IllegalStateException("You forgot to call end()");
        }

        return ((double) (end - start)) / (1000*1000);
    }
}
