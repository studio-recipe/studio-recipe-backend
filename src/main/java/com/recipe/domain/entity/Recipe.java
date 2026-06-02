package com.recipe.domain.entity;

import com.recipe.domain.dto.recipe.RecipeRequestDTO;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "RECIPES")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RCP_SNO")
    private Long rcpSno;

    @Column(name = "RCP_TTL")
    private String rcpTtl;

    @Column(name = "CKG_NM")
    private String ckgNm;

    @Column(name = "INQ_CNT")
    private Integer inqCnt;

    @Column(name = "RCMM_CNT")
    private Integer rcmmCnt;

    @Column(name = "CKG_MTH_ACTO_NM")
    private String ckgMthActoNm;

    @Column(name = "CKG_MTRL_ACTO_NM")
    private String ckgMtrlActoNm;

    @Column(name = "CKG_KND_ACTO_NM")
    private String ckgKndActoNm;

    @Lob
    @Column(name = "CKG_MTRL_CN", columnDefinition = "TEXT")
    private String ckgMtrlCn;

    @Column(name = "CKG_INBUN_NM")
    private String ckgInbunNm;

    @Column(name = "CKG_DODF_NM")
    private String ckgDodfNm;

    @Column(name = "CKG_TIME_NM")
    private String ckgTimeNm;

    @Column(name = "FIRST_REG_DT")
    private LocalDateTime firstRegDt;

    @Column(name = "RCP_IMG_URL")
    private String rcpImgUrl;

    // ── 작성자 연관관계 ─────────────────────────────────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User author;

    // ── 비즈니스 메서드 ─────────────────────────────────────
    public void likeToCountUp() { rcmmCnt++; }

    public void likeToCountDown() {
        if (rcmmCnt > 0) rcmmCnt--;
    }

    public void viewCountUp() {
        if (inqCnt == null) inqCnt = 0;
        inqCnt++;
    }

    public boolean isAuthor(Long userId) {
        return this.author != null && this.author.getUserId().equals(userId);
    }

    public void update(RecipeRequestDTO request) {
        this.rcpTtl        = request.getRcpTtl();
        this.ckgNm         = request.getCkgNm();
        this.ckgMthActoNm  = request.getCkgMthActoNm();
        this.ckgMtrlActoNm = request.getCkgMtrlActoNm();
        this.ckgKndActoNm  = request.getCkgKndActoNm();
        this.ckgMtrlCn     = request.getCkgMtrlCn();
        this.ckgInbunNm    = request.getCkgInbunNm();
        this.ckgDodfNm     = request.getCkgDodfNm();
        this.ckgTimeNm     = request.getCkgTimeNm();
        this.rcpImgUrl     = request.getRcpImgUrl();
    }
}
