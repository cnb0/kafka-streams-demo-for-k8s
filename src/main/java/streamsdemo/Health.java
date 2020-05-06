package streamsdemo;

interface Health {
    class NoHealth implements Health {
        public void start() {
        }
        public void stop() {
        }
    }

    void start();
    void stop();
}
