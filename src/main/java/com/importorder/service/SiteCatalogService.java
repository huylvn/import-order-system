package com.importorder.service;

import com.importorder.dao.DAOFactory;
import com.importorder.dao.ImportSiteDAO;
import com.importorder.dao.MerchandiseDAO;
import com.importorder.dao.SiteMerchandiseDAO;
import com.importorder.model.ImportSite;
import com.importorder.model.Merchandise;
import com.importorder.model.SiteMerchandise;

import java.util.ArrayList;
import java.util.List;

/**
 * Use cases for site merchandise catalog assignments.
 */
public class SiteCatalogService {

    private final SiteMerchandiseDAO siteMerchandiseDAO;
    private final ImportSiteDAO importSiteDAO;
    private final MerchandiseDAO merchandiseDAO;

    public SiteCatalogService(DAOFactory daoFactory) {
        this.siteMerchandiseDAO = daoFactory.createSiteMerchandiseDAO();
        this.importSiteDAO = daoFactory.createImportSiteDAO();
        this.merchandiseDAO = daoFactory.createMerchandiseDAO();
    }

    public SiteMerchandise assignMerchandiseToSite(long siteId, long merchandiseId) {
        ensureSiteExists(siteId);
        ensureMerchandiseExists(merchandiseId);

        if (siteMerchandiseDAO.findBySiteIdAndMerchandiseId(siteId, merchandiseId).isPresent()) {
            throw new ValidationException("Mặt hàng đã được gán cho site này");
        }

        return siteMerchandiseDAO.save(new SiteMerchandise(siteId, merchandiseId));
    }

    public void removeMerchandiseFromSite(long siteId, long merchandiseId) {
        SiteMerchandise link = siteMerchandiseDAO.findBySiteIdAndMerchandiseId(siteId, merchandiseId)
                .orElseThrow(() -> new ValidationException("Không tìm thấy dòng danh mục site"));
        siteMerchandiseDAO.deleteById(link.getId());
    }

    public List<Merchandise> findMerchandiseBySite(long siteId) {
        ensureSiteExists(siteId);
        List<Merchandise> merchandiseList = new ArrayList<>();
        for (SiteMerchandise link : siteMerchandiseDAO.findBySiteId(siteId)) {
            merchandiseDAO.findById(link.getMerchandiseId()).ifPresent(merchandiseList::add);
        }
        return merchandiseList;
    }

    public List<ImportSite> findSitesByMerchandise(long merchandiseId) {
        ensureMerchandiseExists(merchandiseId);
        List<ImportSite> sites = new ArrayList<>();
        for (SiteMerchandise link : siteMerchandiseDAO.findByMerchandiseId(merchandiseId)) {
            importSiteDAO.findById(link.getSiteId()).ifPresent(sites::add);
        }
        return sites;
    }

    private void ensureSiteExists(long siteId) {
        if (importSiteDAO.findById(siteId).isEmpty()) {
            throw new ValidationException("Không tìm thấy site nhập khẩu: " + siteId);
        }
    }

    private void ensureMerchandiseExists(long merchandiseId) {
        if (merchandiseDAO.findById(merchandiseId).isEmpty()) {
            throw new ValidationException("Không tìm thấy mặt hàng: " + merchandiseId);
        }
    }
}
