package com.recipe.domain.entity;

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

    //파싱
    @Lob
    @Column(name = "CKG_MTRL_CN",  columnDefinition = "TEXT")
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

    public void likeToCountUp(){
        rcmmCnt++;
    }

    public void likeToCountDown() {
        if(rcmmCnt > 0)  rcmmCnt--;
    }

    public void viewCountUp() {
        if (inqCnt == null) inqCnt = 0;
        inqCnt++;
    }
}
