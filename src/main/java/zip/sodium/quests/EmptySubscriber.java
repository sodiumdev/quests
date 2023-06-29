package zip.sodium.quests;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class EmptySubscriber<T> implements Subscriber<T> {
    @Override
    public void onSubscribe(Subscription s) {
        s.request(1);
    }

    @Override
    public void onNext(T t) {

    }

    @Override
    public void onError(Throwable t) {

    }

    @Override
    public void onComplete() {

    }
}
