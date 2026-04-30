package product.management.API;

import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = { APIAbstractModule.class, APIModule.class })
public interface APIComponent {

    @Component.Builder
    interface Builder {
        APIComponent build();
    }
}
