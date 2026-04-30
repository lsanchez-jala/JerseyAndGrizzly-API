package product.management.Application;

import dagger.Component;
import jakarta.inject.Singleton;
import product.management.Application.impl.SchemaRegistryService;

@Singleton
@Component(modules = { ApplicationModule.class, ApplicationAbstractModule.class})
public interface ApplicationComponent {

    @Component.Builder
    interface Builder {
        ApplicationComponent build();
    }
}
