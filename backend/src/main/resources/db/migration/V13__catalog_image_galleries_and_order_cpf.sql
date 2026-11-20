ALTER TABLE wand_product
    ADD COLUMN additional_image_urls TEXT[] NOT NULL DEFAULT '{}',
    ADD CONSTRAINT wand_product_image_gallery_limit CHECK (cardinality(additional_image_urls) <= 2);

UPDATE wand_product
SET image_url = '/assets/wands/catalogo/' || slug || '/01.webp',
    additional_image_urls = ARRAY[
        '/assets/wands/catalogo/' || slug || '/02.webp',
        '/assets/wands/catalogo/' || slug || '/03.webp'
    ]
WHERE slug IN (
    'harry-potter-holly', 'tom-riddle-yew', 'hermione-granger-vine',
    'ron-weasley-willow', 'ron-weasley-ash', 'rubeus-hagrid-oak',
    'fleur-delacour-rosewood', 'cedric-diggory-unicorn', 'viktor-krum-hornbeam',
    'bellatrix-lestrange-walnut', 'draco-malfoy-hawthorn', 'peter-pettigrew-chestnut',
    'garrick-ollivander-hornbeam', 'james-potter-mahogany', 'lily-potter-willow',
    'elder-wand', 'neville-longbottom-cherry', 'dolores-umbridge-birch',
    'lucius-malfoy-elm', 'newt-scamander-ash'
) AND customer_owner_id IS NULL;

ALTER TABLE store_order
    ADD COLUMN customer_cpf VARCHAR(11),
    ADD CONSTRAINT store_order_customer_cpf_format CHECK (customer_cpf ~ '^[0-9]{11}$');
