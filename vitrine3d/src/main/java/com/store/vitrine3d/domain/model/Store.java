package com.store.vitrine3d.domain.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "stores")
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false)
    private String storeName;

    @Column(unique = true)
    private String slug;

    private String whatsappNumber;

    // Bate com @Size(max = 500) em StoreRegisterRequest/StoreUpdateRequest — mesma classe de
    // bug corrigida em Product.description: sem @Column, o Hibernate gera varchar(255), e uma
    // descrição de 256-500 chars passa na validação da API mas quebra no INSERT/UPDATE.
    @Column(length = 500)
    private String storeDescription;
    private String logoUrl;
    private String coverImageUrl;

    // Ate 3 fotos informativas (ex.: "Promoção de 50% em camisas") — a loja substitui o
    // conjunto inteiro a cada upload, nao adiciona/remove uma foto isolada.
    @ElementCollection
    @CollectionTable(name = "store_promo_images", joinColumns = @JoinColumn(name = "store_id"))
    @Column(name = "image_url", nullable = false)
    @OrderColumn(name = "image_order")
    private List<String> promoImageUrls = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @Column(nullable = false)
    private Boolean isActive = true;

    private String addressStreet;
    private String addressNumber;
    private String addressNeighborhood;

    @Column(length = 9)
    private String addressZipCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id")
    private State state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_template_id")
    private StoreTemplate storeTemplate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private LayoutMode layoutMode = LayoutMode.LOJA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StoreProfileType profileType = StoreProfileType.STANDARD;

    @Column(nullable = false)
    private boolean emailVerified = false;

    @Column(unique = true, length = 36)
    private String emailVerificationToken;

    private Instant emailVerificationTokenExpiresAt;

    // Personalização visual da loja (fonte, cores) — chaves fixas validadas em
    // StoreThemeUpdateRequest. Visível publicamente: renderiza a mesma pra qualquer visitante.
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "theme_config")
    private Map<String, String> themeConfig = new HashMap<>();

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products = new ArrayList<>();

    public Store() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getWhatsappNumber() { return whatsappNumber; }
    public void setWhatsappNumber(String whatsappNumber) { this.whatsappNumber = whatsappNumber; }

    public String getStoreDescription() { return storeDescription; }
    public void setStoreDescription(String storeDescription) { this.storeDescription = storeDescription; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; }

    public List<String> getPromoImageUrls() { return promoImageUrls; }
    public void setPromoImageUrls(List<String> promoImageUrls) { this.promoImageUrls = promoImageUrls; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public String getAddressStreet() { return addressStreet; }
    public void setAddressStreet(String addressStreet) { this.addressStreet = addressStreet; }

    public String getAddressNumber() { return addressNumber; }
    public void setAddressNumber(String addressNumber) { this.addressNumber = addressNumber; }

    public String getAddressNeighborhood() { return addressNeighborhood; }
    public void setAddressNeighborhood(String addressNeighborhood) { this.addressNeighborhood = addressNeighborhood; }

    public String getAddressZipCode() { return addressZipCode; }
    public void setAddressZipCode(String addressZipCode) { this.addressZipCode = addressZipCode; }

    public State getState() { return state; }
    public void setState(State state) { this.state = state; }

    public City getCity() { return city; }
    public void setCity(City city) { this.city = city; }

    public StoreTemplate getStoreTemplate() { return storeTemplate; }
    public void setStoreTemplate(StoreTemplate storeTemplate) { this.storeTemplate = storeTemplate; }

    public LayoutMode getLayoutMode() { return layoutMode; }
    public void setLayoutMode(LayoutMode layoutMode) { this.layoutMode = layoutMode; }

    public StoreProfileType getProfileType() { return profileType; }
    public void setProfileType(StoreProfileType profileType) { this.profileType = profileType; }

    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

    public String getEmailVerificationToken() { return emailVerificationToken; }
    public void setEmailVerificationToken(String emailVerificationToken) { this.emailVerificationToken = emailVerificationToken; }

    public Instant getEmailVerificationTokenExpiresAt() { return emailVerificationTokenExpiresAt; }
    public void setEmailVerificationTokenExpiresAt(Instant emailVerificationTokenExpiresAt) { this.emailVerificationTokenExpiresAt = emailVerificationTokenExpiresAt; }

    public Map<String, String> getThemeConfig() { return themeConfig; }
    public void setThemeConfig(Map<String, String> themeConfig) { this.themeConfig = themeConfig; }

    public List<Product> getProducts() { return products; }
    public void setProducts(List<Product> products) { this.products = products; }
}
