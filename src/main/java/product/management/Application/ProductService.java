package product.management.Application;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import product.management.Application.exception.ElementNotFoundException;
import product.management.Domain.DTO.Product.ProductDTO;
import product.management.Domain.DTO.Product.ProductRequest;
import product.management.Infrastructure.Mappers.ProductMapper;
import product.management.Infrastructure.Repositories.ProductRepository;

import java.util.List;
import java.util.UUID;

@Singleton
public class ProductService {

    private final ProductRepository repository;
    private final ProductMapper mapper;

    @Inject
    public ProductService(ProductRepository repository, ProductMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<ProductDTO> findAll(){
        return  repository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    public ProductDTO findById(UUID id){
        return mapper.toDto(repository.findById(id));
    }

    public void delete(UUID id){
        if(findById(id) == null){
            throw new ElementNotFoundException("Product with id: "+ id +": doesn't exists.");
        }
        repository.delete(id);
    }

    public ProductDTO save(ProductRequest request){
        if (request == null){
            throw new IllegalArgumentException("The request must not be empty");
        }
        return mapper.toDto(repository.save(request));
    }

    public ProductDTO save(UUID id, ProductRequest request){
        if(findById(id) == null){
            throw new ElementNotFoundException("Product with id: "+ id +": not found.");
        }
        return mapper.toDto(repository.save(id, request));
    }
}
