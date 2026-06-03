package com.importorder.service.delivery;

import com.importorder.factory.AirDeliveryOptionFactory;
import com.importorder.factory.DeliveryOptionFactory;
import com.importorder.factory.ShipDeliveryOptionFactory;
import com.importorder.model.ImportSite;
import com.importorder.model.enums.DeliveryMeans;

import java.util.Objects;
import java.util.Optional;

/**
 * Chooses the best feasible delivery option for a site: ship is preferred over air.
 */
public class DeliveryOptionSelector {

    private final DeliveryOptionFactory shipDeliveryOptionFactory;
    private final DeliveryOptionFactory airDeliveryOptionFactory;

    public DeliveryOptionSelector() {
        this(new ShipDeliveryOptionFactory(), new AirDeliveryOptionFactory());
    }

    public DeliveryOptionSelector(DeliveryOptionFactory shipDeliveryOptionFactory,
                                  DeliveryOptionFactory airDeliveryOptionFactory) {
        this.shipDeliveryOptionFactory = Objects.requireNonNull(
                shipDeliveryOptionFactory, "shipDeliveryOptionFactory must not be null");
        this.airDeliveryOptionFactory = Objects.requireNonNull(
                airDeliveryOptionFactory, "airDeliveryOptionFactory must not be null");
    }

    /**
     * Selects a delivery option when the site can meet the desired delivery date.
     * <ol>
     *   <li>If ship delivery is on time, returns ship.</li>
     *   <li>Else if air delivery is on time, returns air.</li>
     *   <li>Otherwise returns empty.</li>
     * </ol>
     *
     * @param site          import site with transit day configuration
     * @param availableDays calendar days until desired delivery date
     * @return chosen option, or empty when neither method can deliver on time
     */
    public Optional<DeliveryOption> select(ImportSite site, int availableDays) {
        Objects.requireNonNull(site, "site must not be null");

        DeliveryOption shipOption = shipDeliveryOptionFactory.createDeliveryOption(site);
        if (shipOption.canDeliverWithin(availableDays)) {
            return Optional.of(shipOption);
        }

        DeliveryOption airOption = airDeliveryOptionFactory.createDeliveryOption(site);
        if (airOption.canDeliverWithin(availableDays)) {
            return Optional.of(airOption);
        }

        return Optional.empty();
    }

    /**
     * Convenience helper exposing the means of the selected option.
     */
    public Optional<DeliveryMeans> selectDeliveryMeans(ImportSite site, int availableDays) {
        return select(site, availableDays).map(DeliveryOption::getDeliveryMeans);
    }
}
