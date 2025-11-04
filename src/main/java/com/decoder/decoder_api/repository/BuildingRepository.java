package com.decoder.decoder_api.repository;

import com.decoder.decoder_api.model.Building;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Building entity.
 * Provides CRUD operations and ownership checks for access control.
 */
@Repository
public interface BuildingRepository extends JpaRepository<Building, String> {
    // JpaRepository<Building, String>
    //              ^^^^^^^^  ^^^^^^
    //              Entity    Primary key type (String not Long - building IDs are codes)
    
    // Inherited methods:
    // - save(building)
    // - findById(id)
    // - existsById(id)
    // - deleteById(id)
    
    /**
     * Check if a user owns a building - used for RBAC.
     * Spring Data JPA generates SQL from method name automatically!
     * 
     * Method naming convention:
     * - existsBy = returns boolean (true/false)
     * - IdAnd = WHERE id = ? AND
     * - OwnerId = owner_id = ?
     * 
     * Generated SQL:
     * SELECT COUNT(*) > 0 FROM buildings WHERE id = ? AND owner_id = ?
     */
    boolean existsByIdAndOwnerId(String id, String ownerId);
    
    // Example usage:
    // boolean canAccess = buildingRepository.existsByIdAndOwnerId("B001", "owner_john");
    // if (!canAccess) throw new SecurityException("Access denied");
}
