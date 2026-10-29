/** Contratos de la API v1 (espejo de los DTOs del backend). */

export type ElementType =
  | 'BUSINESS_AREA'
  | 'BUSINESS_DOMAIN'
  | 'SERVICE_DOMAIN'
  | 'SERVICE_OPERATION'
  | 'CONTROL_RECORD'
  | 'BUSINESS_OBJECT';

export type ValidationStatus = 'UNVERIFIED' | 'VERIFIED' | 'REJECTED';
export type CatalogSourceType = 'OFFICIAL_IMPORT' | 'EDUCATIONAL_DEMO' | 'USER_IMPORT';
export type RelationshipType =
  | 'CONTAINS'
  | 'HAS_OPERATION'
  | 'HAS_CONTROL_RECORD'
  | 'REFERENCES_BUSINESS_OBJECT'
  | 'COLLABORATES_WITH';
export type RelationshipProvenance = 'SOURCE_DOCUMENTED' | 'EDUCATIONAL_EXAMPLE';

export interface ProblemDetail {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  errors?: { field: string; message: string }[];
}

export interface CurrentUser {
  id: string;
  username: string;
  displayName: string;
  roles: string[];
  permissions: string[];
}

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface CatalogVersion {
  id: string;
  code: string;
  name: string;
  sourceType: CatalogSourceType;
  official: boolean;
  provenanceNote: string;
  standardReleaseLabel?: string;
  status: 'ACTIVE' | 'ARCHIVED';
  importedAt: string;
}

export interface FacetValue {
  value: string;
  count: number;
}

export interface CatalogFacets {
  versionId: string;
  types: FacetValue[];
  categories: FacetValue[];
}

export interface ElementSummary {
  id: string;
  externalRef: string;
  type: ElementType;
  name: string;
  category?: string;
  description?: string;
  validationStatus: ValidationStatus;
}

export interface RelatedElement {
  relationshipId: string;
  relationshipType: RelationshipType;
  direction: 'OUTGOING' | 'INCOMING';
  provenance: RelationshipProvenance;
  description?: string;
  element: ElementSummary;
}

export interface ElementDetail {
  id: string;
  externalRef: string;
  type: ElementType;
  name: string;
  description?: string;
  functionalResponsibility?: string;
  category?: string;
  validationStatus: ValidationStatus;
  sourceReference?: string;
  version: CatalogVersion;
  relationships: RelatedElement[];
}
