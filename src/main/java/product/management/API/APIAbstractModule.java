package product.management.API;

import dagger.Binds;
import dagger.Module;
import product.management.Application.*;
import product.management.Application.impl.*;

@Module
public abstract class APIAbstractModule {
    @Binds
    abstract IOrderService bindOrderService(OrderServiceImpl impl);
    @Binds
    abstract IOrderItemService bindOrderITemService(OrderItemService impl);
    @Binds
    abstract IProductService bindProductService(ProductService impl);
    @Binds
    abstract ICustomerService bindCustomerService(CustomerService impl);
    @Binds
    abstract IShipmentService bindShipmentService(ShipmentService impl);
    @Binds
    abstract IKafkaProducerService bindKafkaProducerService(KafkaProducerService impl);
    @Binds
    abstract ISchemaRegistryService bindSchemaRegistryService(SchemaRegistryService impl);

}