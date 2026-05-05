BEGIN;

-- =========================
-- 1. BASE TABLES
-- =========================

CREATE TABLE IF NOT EXISTS public.roles
(
    id          bigserial PRIMARY KEY,
    name        varchar(255) NOT NULL UNIQUE,
    description text,
    created_at  timestamp without time zone DEFAULT now(),
    updated_at  timestamp without time zone DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.industries
(
    id          bigserial PRIMARY KEY,
    code        varchar(100) NOT NULL UNIQUE,
    name        varchar(255) NOT NULL UNIQUE,
    description text,
    is_active   boolean                     DEFAULT true,
    created_at  timestamp without time zone DEFAULT now(),
    updated_at  timestamp without time zone DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.companies
(
    id                  bigserial PRIMARY KEY,
    name                varchar(255) NOT NULL UNIQUE,
    email               varchar(255) NOT NULL UNIQUE,
    phone_number        varchar(20),
    description         text,
    industry_id         bigint,
    company_size        varchar(50),
    website             varchar(255),
    logo_url            varchar(255),
    logo_public_id      varchar(255),
    tax_code            varchar(50),
    establishment_date  timestamp without time zone,
    is_verified         boolean                     NOT NULL DEFAULT false,
    is_active           boolean                     NOT NULL DEFAULT true,
    created_at          timestamp without time zone DEFAULT now(),
    updated_at          timestamp without time zone DEFAULT now(),
    CONSTRAINT companies_industry_id_fkey FOREIGN KEY (industry_id)
        REFERENCES public.industries (id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS public.addresses
(
    id                       bigserial PRIMARY KEY,
    company_id               bigint,
    verification_address_id  bigint,
    address_type             varchar(50),
    province_code            varchar(50),
    province_name            varchar(100),
    commune_code             varchar(50),
    commune_name             varchar(100),
    detail_address           varchar(200),
    is_primary               boolean                     DEFAULT false,
    is_active                boolean                     DEFAULT true,
    created_at               timestamp without time zone DEFAULT now(),
    updated_at               timestamp without time zone DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.users
(
    id                  bigserial PRIMARY KEY,
    role_id             bigint       NOT NULL,
    company_id          bigint,
    full_name           varchar(255) NOT NULL,
    date_of_birth       date,
    code                varchar(255) NOT NULL UNIQUE,
    gender              varchar(20)
        CHECK (gender IN ('male', 'female', 'other', 'prefer_not_to_say')),
    address_id          bigint,
    email               varchar(255) NOT NULL UNIQUE,
    phone_number        varchar(20),
    password_hash       varchar(255) NOT NULL,
    avatar_url          text,
    avatar_public_id    varchar(255),
    is_active           boolean                     NOT NULL DEFAULT true,
    is_email_verified   boolean                     DEFAULT false,
    verification_token  varchar(255),
    token_expiry_date   timestamp without time zone,
    last_login_at       timestamp without time zone,
    created_at          timestamp without time zone DEFAULT now(),
    updated_at          timestamp without time zone DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.job_categories
(
    id          bigserial PRIMARY KEY,
    name        varchar(255) NOT NULL UNIQUE,
    description text,
    parent_id   bigint,
    created_at  timestamp without time zone DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.resumes
(
    id             bigserial PRIMARY KEY,
    user_id        bigint       NOT NULL,
    title          varchar(255) NOT NULL,
    file_url       varchar(500),
    file_name      varchar(255),
    file_size      bigint,
    file_type      varchar(50),
    file_public_id text,
    summary        text,
    is_primary     boolean DEFAULT false,
    is_public      boolean DEFAULT false,
    created_at     timestamp without time zone DEFAULT now(),
    updated_at     timestamp without time zone DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.jobs
(
    id                    bigserial PRIMARY KEY,
    created_by            bigint       NOT NULL,
    company_id            bigint       NOT NULL,
    title                 varchar(255) NOT NULL,
    description           text         NOT NULL,
    requirements          text,
    responsibilities      text,
    benefits              text,
    address_id            bigint,
    work_type             varchar(50)
        CHECK (work_type IN ('remote', 'onsite', 'hybrid')),
    employment_type       varchar(50)
        CHECK (employment_type IN ('full_time', 'part_time', 'intern', 'contract', 'freelance')),
    experience_level      varchar(50)
        CHECK (experience_level IN ('intern', 'fresher', 'junior', 'mid', 'senior', 'lead')),
    is_salary_negotiable  boolean        NOT NULL DEFAULT false,
    salary_min            numeric(12, 2) CHECK (salary_min >= 0),
    salary_max            numeric(12, 2) CHECK (salary_max IS NULL OR salary_min IS NULL OR salary_max >= salary_min),
    salary_currency       varchar(3)     DEFAULT 'VND',
    number_of_positions   integer        DEFAULT 1 CHECK (number_of_positions > 0),
    application_deadline  timestamp without time zone,
    status                varchar(50)    DEFAULT 'draft'
        CHECK (status IN ('draft', 'published', 'closed')),
    views_count           integer        DEFAULT 0 CHECK (views_count >= 0),
    applications_count    integer        DEFAULT 0 CHECK (applications_count >= 0),
    is_featured           boolean        DEFAULT false,
    published_at          timestamp without time zone,
    closed_at             timestamp without time zone,
    created_at            timestamp without time zone DEFAULT now(),
    updated_at            timestamp without time zone DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.skills
(
    id   bigserial PRIMARY KEY,
    name varchar(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS public.job_skills
(
    job_id   bigint NOT NULL,
    skill_id bigint NOT NULL,
    PRIMARY KEY (job_id, skill_id)
);

CREATE TABLE IF NOT EXISTS public.job_category_mapping
(
    job_id      bigint NOT NULL,
    category_id bigint NOT NULL,
    PRIMARY KEY (job_id, category_id)
);

CREATE TABLE IF NOT EXISTS public.resume_education
(
    id             bigserial PRIMARY KEY,
    resume_id      bigint NOT NULL,
    institution    varchar(255),
    degree         varchar(255),
    field_of_study varchar(255),
    start_date     date,
    end_date       date,
    gpa            numeric(3, 2) CHECK (gpa >= 0),
    description    text,
    display_order  integer DEFAULT 0 CHECK (display_order >= 0),
    CHECK (start_date IS NULL OR end_date IS NULL OR start_date <= end_date)
);

CREATE TABLE IF NOT EXISTS public.resume_experiences
(
    id            bigserial PRIMARY KEY,
    resume_id     bigint NOT NULL,
    company_name  varchar(255),
    position      varchar(255),
    description   text,
    start_date    date,
    end_date      date,
    is_current    boolean DEFAULT false,
    display_order integer DEFAULT 0 CHECK (display_order >= 0),
    CHECK (start_date IS NULL OR end_date IS NULL OR start_date <= end_date)
);

CREATE TABLE IF NOT EXISTS public.resume_skills
(
    id                  bigserial PRIMARY KEY,
    resume_id           bigint NOT NULL,
    skill_name          varchar(100),
    proficiency_level   varchar(50),
    years_of_experience integer CHECK (years_of_experience >= 0),
    CONSTRAINT uq_resume_skill UNIQUE (resume_id, skill_name)
);

CREATE TABLE IF NOT EXISTS public.applications
(
    id           bigserial PRIMARY KEY,
    job_id       bigint NOT NULL,
    user_id      bigint NOT NULL,
    resume_id    bigint,
    cover_letter text,
    status       varchar(50) DEFAULT 'pending'
        CHECK (status IN ('pending', 'reviewing', 'accepted', 'rejected')),
    applied_at   timestamp without time zone DEFAULT now(),
    reviewed_at  timestamp without time zone,
    reviewed_by  bigint,
    notes        text,
    rating       integer CHECK (rating BETWEEN 1 AND 5),
    created_at   timestamp without time zone DEFAULT now(),
    updated_at   timestamp without time zone DEFAULT now(),
    CONSTRAINT unique_user_job_application UNIQUE (user_id, job_id)
);

CREATE TABLE IF NOT EXISTS public.application_status_history
(
    id             bigserial PRIMARY KEY,
    application_id bigint NOT NULL,
    old_status     varchar(50),
    new_status     varchar(50) NOT NULL,
    changed_by     bigint,
    notes          text,
    changed_at     timestamp without time zone DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.company_invitations
(
    id         bigserial PRIMARY KEY,
    company_id bigint      NOT NULL,
    created_by bigint      NOT NULL,
    code       varchar(50) NOT NULL UNIQUE,
    role       varchar(50) NOT NULL DEFAULT 'HR'
        CHECK (role IN ('hr', 'HR', 'admin', 'recruiter')),
    email      varchar(100),
    max_uses   integer                     DEFAULT 1 CHECK (max_uses > 0),
    used_count integer                     DEFAULT 0 CHECK (used_count >= 0),
    expires_at timestamp without time zone NOT NULL,
    is_active  boolean                     DEFAULT true,
    created_at timestamp without time zone DEFAULT now(),
    CHECK (used_count <= max_uses)
);

CREATE TABLE IF NOT EXISTS public.company_verification_requests
(
    id                 bigserial PRIMARY KEY,
    user_id            bigint       NOT NULL,
    company_id         bigint,
    company_name       varchar(255) NOT NULL UNIQUE,
    tax_code           varchar(50)  NOT NULL UNIQUE,
    contact_person     varchar(100),
    contact_phone      varchar(20),
    contact_email      varchar(100),
    website            varchar(255),
    description        text,
    company_size       varchar(50),
    establishment_date timestamp without time zone,
    industry_id        bigint,
    status             varchar(50) DEFAULT 'pending'
        CHECK (status IN ('pending', 'approved', 'rejected')),
    admin_notes        text,
    rejection_reason   varchar(500),
    reviewed_by        bigint,
    reviewed_at        timestamp without time zone,
    logo_url           varchar(500),
    logo_public_id     text,
    created_at         timestamp without time zone DEFAULT now(),
    updated_at         timestamp without time zone DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.company_verification_documents
(
    id                      bigserial PRIMARY KEY,
    verification_request_id bigint       NOT NULL,
    file_url                varchar(500) NOT NULL,
    file_type               varchar(50),
    public_id               text,
    created_at              timestamp without time zone DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.notifications
(
    id             bigserial PRIMARY KEY,
    user_id        bigint       NOT NULL,
    title          varchar(255) NOT NULL,
    message        text,
    type           varchar(50)
        CHECK (type IN (
            'application_submitted', 'application_status_update',
            'job_expired', 'job_created', 'job_updated', 'job_status_changed',
            'job_published', 'job_saved',
            'category_created', 'category_updated', 'category_deleted',
            'company_verify_requested', 'company_verified', 'company_rejected', 'company_status_changed',
            'resume_created', 'resume_updated', 'resume_deleted', 'resume_primary_changed', 'resume_visibility_changed',
            'system'
        )),
    reference_id   bigint,
    reference_type varchar(50),
    is_read        boolean DEFAULT false,
    created_at     timestamp without time zone DEFAULT now()
);


CREATE TABLE IF NOT EXISTS public.refresh_tokens
(
    id         bigserial PRIMARY KEY,
    user_id    bigint NOT NULL,
    token      text   NOT NULL UNIQUE,
    expires_at timestamp without time zone NOT NULL,
    is_revoked boolean                     NOT NULL DEFAULT false,
    revoked_at timestamp without time zone,
    created_at timestamp without time zone NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS public.saved_jobs
(
    id         bigserial PRIMARY KEY,
    user_id    bigint NOT NULL,
    job_id     bigint NOT NULL,
    created_at timestamp without time zone DEFAULT now(),
    CONSTRAINT unique_user_saved_job UNIQUE (user_id, job_id)
);

CREATE TABLE IF NOT EXISTS public.user_providers
(
    id               bigserial PRIMARY KEY,
    user_id          bigint      NOT NULL,
    provider_name    varchar(50) NOT NULL,
    provider_id      varchar(255) NOT NULL,
    access_token     text,
    refresh_token    text,
    token_expires_at timestamp without time zone,
    created_at       timestamp without time zone DEFAULT now(),
    CONSTRAINT unique_user_provider UNIQUE (user_id, provider_name)
);

-- =========================
-- 2. FOREIGN KEYS
-- =========================

ALTER TABLE IF EXISTS public.addresses
    ADD CONSTRAINT fk_addresses_company
        FOREIGN KEY (company_id)
            REFERENCES public.companies (id) ON DELETE CASCADE;

ALTER TABLE IF EXISTS public.addresses
    ADD CONSTRAINT fk_addresses_verification_request
        FOREIGN KEY (verification_address_id)
            REFERENCES public.company_verification_requests (id) ON DELETE SET NULL;

ALTER TABLE IF EXISTS public.users
    ADD CONSTRAINT users_role_id_fkey
        FOREIGN KEY (role_id)
            REFERENCES public.roles (id) ON DELETE NO ACTION;

ALTER TABLE IF EXISTS public.users
    ADD CONSTRAINT users_company_id_fkey
        FOREIGN KEY (company_id)
            REFERENCES public.companies (id) ON DELETE SET NULL;

ALTER TABLE IF EXISTS public.users
    ADD CONSTRAINT users_address_id_fkey
        FOREIGN KEY (address_id)
            REFERENCES public.addresses (id) ON DELETE NO ACTION;

ALTER TABLE IF EXISTS public.job_categories
    ADD CONSTRAINT job_categories_parent_id_fkey
        FOREIGN KEY (parent_id)
            REFERENCES public.job_categories (id) ON DELETE SET NULL;

ALTER TABLE IF EXISTS public.resumes
    ADD CONSTRAINT resumes_user_id_fkey
        FOREIGN KEY (user_id)
            REFERENCES public.users (id) ON DELETE CASCADE;

ALTER TABLE IF EXISTS public.jobs
    ADD CONSTRAINT jobs_company_id_fkey
        FOREIGN KEY (company_id)
            REFERENCES public.companies (id) ON DELETE CASCADE;

ALTER TABLE IF EXISTS public.jobs
    ADD CONSTRAINT jobs_created_by_fkey
        FOREIGN KEY (created_by)
            REFERENCES public.users (id) ON DELETE NO ACTION;

ALTER TABLE IF EXISTS public.jobs
    ADD CONSTRAINT jobs_address_id_fkey
        FOREIGN KEY (address_id)
            REFERENCES public.addresses (id) ON DELETE NO ACTION;

ALTER TABLE IF EXISTS public.job_skills
    ADD CONSTRAINT job_skills_job_id_fkey
        FOREIGN KEY (job_id)
            REFERENCES public.jobs (id) ON DELETE CASCADE;

ALTER TABLE IF EXISTS public.job_skills
    ADD CONSTRAINT job_skills_skill_id_fkey
        FOREIGN KEY (skill_id)
            REFERENCES public.skills (id) ON DELETE CASCADE;

ALTER TABLE IF EXISTS public.job_category_mapping
    ADD CONSTRAINT job_category_mapping_job_id_fkey
        FOREIGN KEY (job_id)
            REFERENCES public.jobs (id) ON DELETE CASCADE;

ALTER TABLE IF EXISTS public.job_category_mapping
    ADD CONSTRAINT job_category_mapping_category_id_fkey
        FOREIGN KEY (category_id)
            REFERENCES public.job_categories (id) ON DELETE CASCADE;

ALTER TABLE IF EXISTS public.resume_education
    ADD CONSTRAINT resume_education_resume_id_fkey
        FOREIGN KEY (resume_id)
            REFERENCES public.resumes (id) ON DELETE CASCADE;

ALTER TABLE IF EXISTS public.resume_experiences
    ADD CONSTRAINT resume_experiences_resume_id_fkey
        FOREIGN KEY (resume_id)
            REFERENCES public.resumes (id) ON DELETE CASCADE;

ALTER TABLE IF EXISTS public.resume_skills
    ADD CONSTRAINT resume_skills_resume_id_fkey
        FOREIGN KEY (resume_id)
            REFERENCES public.resumes (id) ON DELETE CASCADE;

ALTER TABLE IF EXISTS public.applications
    ADD CONSTRAINT applications_job_id_fkey
        FOREIGN KEY (job_id)
            REFERENCES public.jobs (id) ON DELETE CASCADE;

ALTER TABLE IF EXISTS public.applications
    ADD CONSTRAINT applications_user_id_fkey
        FOREIGN KEY (user_id)
            REFERENCES public.users (id) ON DELETE CASCADE;

ALTER TABLE IF EXISTS public.applications
    ADD CONSTRAINT applications_resume_id_fkey
        FOREIGN KEY (resume_id)
            REFERENCES public.resumes (id) ON DELETE SET NULL;

ALTER TABLE IF EXISTS public.applications
    ADD CONSTRAINT applications_reviewed_by_fkey
        FOREIGN KEY (reviewed_by)
            REFERENCES public.users (id) ON DELETE NO ACTION;

ALTER TABLE IF EXISTS public.application_status_history
    ADD CONSTRAINT application_status_history_application_id_fkey
        FOREIGN KEY (application_id)
            REFERENCES public.applications (id) ON DELETE CASCADE;

ALTER TABLE IF EXISTS public.application_status_history
    ADD CONSTRAINT application_status_history_changed_by_fkey
        FOREIGN KEY (changed_by)
            REFERENCES public.users (id) ON DELETE NO ACTION;

ALTER TABLE IF EXISTS public.company_invitations
    ADD CONSTRAINT company_invitations_company_id_fkey
        FOREIGN KEY (company_id)
            REFERENCES public.companies (id) ON DELETE CASCADE;

ALTER TABLE IF EXISTS public.company_invitations
    ADD CONSTRAINT company_invitations_created_by_fkey
        FOREIGN KEY (created_by)
            REFERENCES public.users (id) ON DELETE NO ACTION;

ALTER TABLE IF EXISTS public.company_verification_requests
    ADD CONSTRAINT company_verification_requests_user_id_fkey
        FOREIGN KEY (user_id)
            REFERENCES public.users (id) ON DELETE CASCADE;

ALTER TABLE IF EXISTS public.company_verification_requests
    ADD CONSTRAINT company_verification_requests_company_id_fkey
        FOREIGN KEY (company_id)
            REFERENCES public.companies (id) ON DELETE CASCADE;

ALTER TABLE IF EXISTS public.company_verification_requests
    ADD CONSTRAINT company_verification_requests_reviewed_by_fkey
        FOREIGN KEY (reviewed_by)
            REFERENCES public.users (id) ON DELETE NO ACTION;

ALTER TABLE IF EXISTS public.company_verification_requests
    ADD CONSTRAINT company_verification_requests_industry_id_fkey
        FOREIGN KEY (industry_id)
            REFERENCES public.industries (id) ON DELETE SET NULL;

ALTER TABLE IF EXISTS public.company_verification_documents
    ADD CONSTRAINT company_verification_documents_request_id_fkey
        FOREIGN KEY (verification_request_id)
            REFERENCES public.company_verification_requests (id) ON DELETE CASCADE;

ALTER TABLE IF EXISTS public.notifications
    ADD CONSTRAINT notifications_user_id_fkey
        FOREIGN KEY (user_id)
            REFERENCES public.users (id) ON DELETE CASCADE;

ALTER TABLE IF EXISTS public.refresh_tokens
    ADD CONSTRAINT refresh_tokens_user_id_fkey
        FOREIGN KEY (user_id)
            REFERENCES public.users (id) ON DELETE CASCADE;

ALTER TABLE IF EXISTS public.saved_jobs
    ADD CONSTRAINT saved_jobs_user_id_fkey
        FOREIGN KEY (user_id)
            REFERENCES public.users (id) ON DELETE CASCADE;

ALTER TABLE IF EXISTS public.saved_jobs
    ADD CONSTRAINT saved_jobs_job_id_fkey
        FOREIGN KEY (job_id)
            REFERENCES public.jobs (id) ON DELETE CASCADE;

ALTER TABLE IF EXISTS public.user_providers
    ADD CONSTRAINT user_providers_user_id_fkey
        FOREIGN KEY (user_id)
            REFERENCES public.users (id) ON DELETE CASCADE;

-- =========================
-- 3. INDEXES
-- =========================

-- Basic single-column indexes
CREATE INDEX IF NOT EXISTS idx_users_role_id ON public.users (role_id);
CREATE INDEX IF NOT EXISTS idx_users_company_id ON public.users (company_id);
CREATE INDEX IF NOT EXISTS idx_jobs_company_id ON public.jobs (company_id);
CREATE INDEX IF NOT EXISTS idx_jobs_created_by ON public.jobs (created_by);
CREATE INDEX IF NOT EXISTS idx_jobs_status ON public.jobs (status);
CREATE INDEX IF NOT EXISTS idx_jobs_published_at ON public.jobs (published_at);
CREATE INDEX IF NOT EXISTS idx_jobs_deadline ON public.jobs (application_deadline);
CREATE INDEX IF NOT EXISTS idx_applications_job_id ON public.applications (job_id);
CREATE INDEX IF NOT EXISTS idx_applications_user_id ON public.applications (user_id);
CREATE INDEX IF NOT EXISTS idx_applications_status ON public.applications (status);
CREATE INDEX IF NOT EXISTS idx_applications_applied_at ON public.applications (applied_at);
CREATE INDEX IF NOT EXISTS idx_app_status_history_app_id ON public.application_status_history (application_id);
CREATE INDEX IF NOT EXISTS idx_invitations_company_id ON public.company_invitations (company_id);
CREATE INDEX IF NOT EXISTS idx_verification_user_id ON public.company_verification_requests (user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON public.notifications (user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_user_unread ON public.notifications (user_id, is_read, created_at);
CREATE INDEX IF NOT EXISTS idx_tokens_user_id ON public.refresh_tokens (user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_revoked ON public.refresh_tokens (user_id, is_revoked);
CREATE INDEX IF NOT EXISTS idx_resumes_user_id ON public.resumes (user_id);
CREATE INDEX IF NOT EXISTS idx_saved_jobs_user_id ON public.saved_jobs (user_id);
CREATE INDEX IF NOT EXISTS idx_saved_jobs_job_id ON public.saved_jobs (job_id);
CREATE INDEX IF NOT EXISTS idx_companies_industry_id ON public.companies (industry_id);
CREATE INDEX IF NOT EXISTS idx_addresses_verification_id ON public.addresses (verification_address_id);

-- Composite indexes for Dashboard Statistics (V001 Migration)
-- These indexes optimize aggregation queries for Admin, HR, and Company Admin dashboards
CREATE INDEX IF NOT EXISTS idx_jobs_company_status ON public.jobs (company_id, status);
CREATE INDEX IF NOT EXISTS idx_jobs_created_status ON public.jobs (created_at, status);
CREATE INDEX IF NOT EXISTS idx_users_company_role_active ON public.users (company_id, role_id, is_active);
CREATE INDEX IF NOT EXISTS idx_users_created_active ON public.users (created_at, is_active);
CREATE INDEX IF NOT EXISTS idx_companies_created_active ON public.companies (created_at, is_active);

-- =========================
-- 4. PARTIAL UNIQUE INDEXES
-- =========================

CREATE UNIQUE INDEX IF NOT EXISTS ux_resumes_one_primary_per_user
    ON public.resumes (user_id) WHERE is_primary = true;

CREATE UNIQUE INDEX IF NOT EXISTS ux_addresses_one_primary_per_company
    ON public.addresses (company_id) WHERE is_primary = true;
CREATE INDEX idx_jobs_company_status ON jobs (company_id, status) WHERE is_active = true;
CREATE INDEX idx_users_company_role_active ON users (company_id, role_id, is_active);
CREATE INDEX idx_jobs_created_status ON jobs (created_at, status);
CREATE INDEX idx_users_created_active ON users (created_at, is_active);
CREATE INDEX idx_companies_created_active ON companies (created_at, is_active);

-- =========================
-- 5. UPDATED_AT TRIGGER
-- =========================

CREATE OR REPLACE FUNCTION set_updated_at()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_roles_updated
    BEFORE UPDATE ON public.roles
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_industries_updated
    BEFORE UPDATE ON public.industries
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_companies_updated
    BEFORE UPDATE ON public.companies
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_addresses_updated
    BEFORE UPDATE ON public.addresses
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_users_updated
    BEFORE UPDATE ON public.users
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_resumes_updated
    BEFORE UPDATE ON public.resumes
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_jobs_updated
    BEFORE UPDATE ON public.jobs
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_applications_updated
    BEFORE UPDATE ON public.applications
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_company_verification_requests_updated
    BEFORE UPDATE ON public.company_verification_requests
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

-- =========================
-- 6. SEED DATA
-- =========================

INSERT INTO public.industries (code, name)
VALUES ('software', 'Software'),
       ('finance', 'Finance'),
       ('education', 'Education'),
       ('healthcare', 'Healthcare'),
       ('ecommerce', 'E-commerce'),
       ('manufacturing', 'Manufacturing'),
       ('marketing', 'Marketing'),
       ('logistics', 'Logistics')
ON CONFLICT (code) DO NOTHING;

INSERT INTO public.roles (name, description)
VALUES ('ADMIN', 'System administrator'),
       ('COMPANY_ADMIN', 'Company administrator'),
       ('HR', 'Human resources staff'),
       ('CANDIDATE', 'Job seeker / candidate')
ON CONFLICT (name) DO NOTHING;

COMMIT;
